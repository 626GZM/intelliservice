package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.KnowledgeDocumentMapper;
import com.intelliservice.backend.model.entity.KnowledgeDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "md", "txt");

    private final KnowledgeDocumentMapper documentMapper;
    private final WebClient agentWebClient;

    @Value("${knowledge.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        log.info("文件上传目录: {}", Paths.get(uploadDir).toAbsolutePath());
    }

    // ------------------------------------------------------------------ //

    public KnowledgeDocument uploadDocument(MultipartFile file, Long userId) throws IOException {
        // 1. 校验文件类型
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "unknown";
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension + "，仅支持 PDF、MD、TXT");
        }

        // 2. 保存文件到磁盘（用 UUID 避免重名）
        String storedName = UUID.randomUUID() + "." + extension;
        Path filePath = Paths.get(uploadDir, storedName);
        Files.copy(file.getInputStream(), filePath);

        // 3. 写入数据库，状态 processing
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setUserId(userId);
        doc.setFilename(originalFilename);
        doc.setFilePath(filePath.toString());
        doc.setFileSize(file.getSize());
        doc.setStatus("processing");
        doc.setChunkCount(0);
        doc.setCreatedAt(LocalDateTime.now());
        documentMapper.insert(doc);
        Long docId = doc.getId();

        // 4. 异步调用 Python Agent 做向量化（非阻塞，通过 subscribe 回调更新状态）
        Map<String, Object> body = Map.of(
                "doc_id", docId,
                "file_path", filePath.toAbsolutePath().toString(),
                "filename", originalFilename
        );
        agentWebClient.post()
                .uri("/agent/knowledge/upload")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                        resp -> {
                            int chunkCount = resp.containsKey("chunk_count")
                                    ? ((Number) resp.get("chunk_count")).intValue() : 0;
                            updateStatus(docId, "ready", chunkCount);
                            log.info("文档 {} 向量化完成，共 {} 个文本块", docId, chunkCount);
                        },
                        err -> {
                            updateStatus(docId, "failed", 0);
                            log.error("文档 {} 向量化失败: {}", docId, err.getMessage());
                        }
                );

        return doc;
    }

    public List<KnowledgeDocument> listDocuments(Long userId) {
        return documentMapper.selectByUserId(userId);
    }

    public void deleteDocument(Long docId) {
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw new RuntimeException("文档不存在");
        }

        // 通知 Python Agent 删除对应向量（忽略失败，继续删除本地）
        agentWebClient.post()
                .uri("/agent/knowledge/delete")
                .bodyValue(Map.of("doc_id", docId))
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                        resp -> log.info("文档 {} 向量已从 Agent 删除", docId),
                        err -> log.warn("通知 Agent 删除文档 {} 失败（已继续删除本地记录）: {}", docId, err.getMessage())
                );

        // 删除本地文件
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException e) {
            log.warn("删除本地文件失败: {}", e.getMessage());
        }

        // 删除数据库记录
        documentMapper.deleteById(docId);
    }

    // ------------------------------------------------------------------ //

    private void updateStatus(Long docId, String status, int chunkCount) {
        KnowledgeDocument update = new KnowledgeDocument();
        update.setId(docId);
        update.setStatus(status);
        update.setChunkCount(chunkCount);
        documentMapper.updateById(update);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }
}
