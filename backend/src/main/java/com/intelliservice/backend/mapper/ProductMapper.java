package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM products ORDER BY id ASC")
    List<Product> selectAll();

    @Select("SELECT * FROM products WHERE category = #{category} ORDER BY id ASC")
    List<Product> selectByCategory(String category);

    @Select("SELECT * FROM products WHERE id != #{id} AND category = #{category} ORDER BY id ASC LIMIT #{limit}")
    List<Product> selectSimilar(@Param("id") Long id, @Param("category") String category, @Param("limit") int limit);

    @Select("SELECT * FROM products WHERE name LIKE CONCAT('%', #{keyword}, '%') ORDER BY id ASC")
    List<Product> searchByKeyword(String keyword);
}
