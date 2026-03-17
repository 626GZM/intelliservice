package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Ticket;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TicketMapper extends BaseMapper<Ticket> {

    @Select("SELECT * FROM tickets WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Ticket> selectByUserIdOrderByCreatedAtDesc(Long userId);
}
