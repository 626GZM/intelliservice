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
}
