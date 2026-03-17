package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Message;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM messages WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<Message> selectBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
