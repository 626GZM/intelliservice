package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Order;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> selectByUserId(Long userId);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} LIMIT 1")
    Order selectByOrderNo(String orderNo);
}
