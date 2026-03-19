package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.Product;
import com.intelliservice.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 商品列表，支持 ?category=手机壳 过滤 */
    @GetMapping
    public ApiResponse<List<Product>> list(@RequestParam(required = false) String category) {
        return ApiResponse.success(productService.listAll(category));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public ApiResponse<Product> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    /** 相似商品推荐（同 category，最多5个） */
    @GetMapping("/{id}/similar")
    public ApiResponse<List<Product>> similar(@PathVariable Long id) {
        return ApiResponse.success(productService.getSimilar(id));
    }
}
