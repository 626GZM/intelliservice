package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Rating;
import org.apache.ibatis.annotations.Select;

public interface RatingMapper extends BaseMapper<Rating> {

    @Select("SELECT * FROM ratings WHERE session_id = #{sessionId} LIMIT 1")
    Rating selectBySessionId(Long sessionId);
}
