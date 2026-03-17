package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Session;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    @Select("SELECT * FROM sessions WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Session> selectByUserIdOrderByUpdatedAtDesc(Long userId);
}
