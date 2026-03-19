package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.SubmitRatingRequest;
import com.intelliservice.backend.model.entity.Rating;
import com.intelliservice.backend.service.RatingService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    /** 提交评价 */
    @PostMapping
    public ApiResponse<Rating> submit(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody SubmitRatingRequest req) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(ratingService.submitRating(userId, req));
    }

    /** 查看某会话的评价 */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<Rating> getBySession(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long sessionId) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(ratingService.getBySessionId(userId, sessionId));
    }
}
