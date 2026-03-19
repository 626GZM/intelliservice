package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.KnowledgeDocument;
import com.intelliservice.backend.service.KnowledgeService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final UserService userService;

    @PostMapping("/upload")
    public ApiResponse<KnowledgeDocument> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        KnowledgeDocument doc = knowledgeService.uploadDocument(file, userId);
        return ApiResponse.success(doc);
    }

    @GetMapping("/documents")
    public ApiResponse<List<KnowledgeDocument>> list(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(knowledgeService.listDocuments(userId));
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteDocument(id);
        return ApiResponse.success(null);
    }

    /**
     * Python Agent 回调接口：文档向量化完成后更新状态。
     * 路径 /api/knowledge/documents/{id}/status 已在 SecurityConfig 放行，无需 JWT。
     */
    @PutMapping("/documents/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @RequestBody java.util.Map<String, Object> body) {
        String status = (String) body.getOrDefault("status", "ready");
        int chunkCount = body.containsKey("chunk_count")
                ? ((Number) body.get("chunk_count")).intValue() : 0;
        knowledgeService.updateDocumentStatus(id, status, chunkCount);
        return ApiResponse.success(null);
    }
}
