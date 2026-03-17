package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    @Select("SELECT * FROM knowledge_documents WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<KnowledgeDocument> selectByUserId(Long userId);
}
