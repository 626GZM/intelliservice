package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.RatingMapper;
import com.intelliservice.backend.model.dto.SubmitRatingRequest;
import com.intelliservice.backend.model.entity.Rating;
import com.intelliservice.backend.model.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingMapper ratingMapper;
    private final SessionService sessionService;

    private static final Set<String> ALLOWED_RATINGS = Set.of("good", "neutral", "bad");

    public Rating submitRating(Long userId, SubmitRatingRequest req) {
        if (req.getSessionId() == null) {
            throw new RuntimeException("缺少 sessionId 参数");
        }
        Session session = sessionService.getSession(req.getSessionId());
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价该会话");
        }
        if (ratingMapper.selectBySessionId(req.getSessionId()) != null) {
            throw new RuntimeException("已评价");
        }

        String ratingValue = req.getRating() != null ? req.getRating() : "good";
        if (!ALLOWED_RATINGS.contains(ratingValue)) {
            throw new RuntimeException("rating 仅支持 good/neutral/bad");
        }

        Rating rating = new Rating();
        rating.setSessionId(req.getSessionId());
        rating.setUserId(userId);
        rating.setRating(ratingValue);
        rating.setComment(req.getComment());
        rating.setAutoRated(0);
        rating.setCreatedAt(LocalDateTime.now());
        ratingMapper.insert(rating);
        return rating;
    }

    public Rating getBySessionId(Long userId, Long sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看该会话评价");
        }
        return ratingMapper.selectBySessionId(sessionId);
    }

    public int autoRateClosedSessions(List<Session> sessions) {
        int inserted = 0;
        for (Session session : sessions) {
            if (ratingMapper.selectBySessionId(session.getId()) != null) {
                continue;
            }
            Rating rating = new Rating();
            rating.setSessionId(session.getId());
            rating.setUserId(session.getUserId());
            rating.setRating("good");
            rating.setComment(null);
            rating.setAutoRated(1);
            rating.setCreatedAt(LocalDateTime.now());
            ratingMapper.insert(rating);
            inserted++;
        }
        return inserted;
    }
}
