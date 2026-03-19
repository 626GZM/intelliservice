package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.dto.TransferServingSessionResponse;
import com.intelliservice.backend.model.dto.TransferWaitingSessionResponse;
import com.intelliservice.backend.model.entity.Session;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    @Select("SELECT * FROM sessions WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Session> selectByUserIdOrderByUpdatedAtDesc(Long userId);

    @Select("SELECT COUNT(DISTINCT user_id) FROM sessions WHERE DATE(updated_at) = CURDATE()")
    int countActiveTodayUsers();

    @Select("SELECT * FROM sessions WHERE transfer_status = #{status} ORDER BY updated_at ASC")
    List<Session> selectByTransferStatus(String status);

    @Select("""
            SELECT s.id AS session_id,
                   u.username,
                   s.context_type,
                   s.context_id,
                   (
                       SELECT m.content
                       FROM messages m
                       WHERE m.session_id = s.id
                       ORDER BY m.created_at DESC, m.id DESC
                       LIMIT 1
                   ) AS last_message_content,
                   s.updated_at AS waiting_since,
                   TIMESTAMPDIFF(SECOND, s.updated_at, NOW()) AS waiting_seconds
            FROM sessions s
            JOIN users u ON u.id = s.user_id
            WHERE s.transfer_status = 'waiting'
            ORDER BY s.updated_at ASC
            """)
    List<TransferWaitingSessionResponse> selectWaitingSessionSummaries();

    @Select("""
            SELECT s.id AS session_id,
                   u.username,
                   s.context_type,
                   s.context_id,
                   (
                       SELECT m.content
                       FROM messages m
                       WHERE m.session_id = s.id
                       ORDER BY m.created_at DESC, m.id DESC
                       LIMIT 1
                   ) AS last_message_content,
                   s.updated_at AS serving_since
            FROM sessions s
            JOIN users u ON u.id = s.user_id
            WHERE s.transfer_status = 'serving'
              AND s.agent_id = #{agentId}
            ORDER BY s.updated_at DESC, s.id DESC
            """)
    List<TransferServingSessionResponse> selectServingSessionSummaries(Long agentId);

    @Select("""
            SELECT s.*
            FROM sessions s
            WHERE (s.status = 'closed' OR s.transfer_status = 'closed')
              AND s.created_at <= DATE_SUB(NOW(), INTERVAL 24 HOUR)
              AND NOT EXISTS (
                  SELECT 1
                  FROM ratings r
                  WHERE r.session_id = s.id
              )
            ORDER BY s.created_at ASC
            """)
    List<Session> selectClosedSessionsWithoutRating();
}
