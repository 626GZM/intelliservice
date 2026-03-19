package com.intelliservice.backend.task;

import com.intelliservice.backend.mapper.SessionMapper;
import com.intelliservice.backend.model.entity.Session;
import com.intelliservice.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SessionMapper sessionMapper;
    private final RatingService ratingService;

    @Scheduled(cron = "0 0 * * * *")
    public void autoRateClosedSessions() {
        List<Session> sessions = sessionMapper.selectClosedSessionsWithoutRating();
        if (sessions.isEmpty()) {
            return;
        }
        int inserted = ratingService.autoRateClosedSessions(sessions);
        log.info("自动好评完成 count={}", inserted);
    }
}
