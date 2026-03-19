package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.ProductMapper;
import com.intelliservice.backend.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public List<Product> listAll(String category) {
        if (category != null && !category.isBlank()) {
            return productMapper.selectByCategory(category);
        }
        return productMapper.selectAll();
    }

    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    public List<Product> getSimilar(Long id) {
        Product product = getById(id);
        return productMapper.selectSimilar(id, product.getCategory(), 5);
    }

    public List<Product> search(String keyword) {
        return productMapper.searchByKeyword(keyword);
    }
}
