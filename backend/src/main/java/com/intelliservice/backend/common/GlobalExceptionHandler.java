package com.intelliservice.backend.common;

import com.intelliservice.backend.model.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（用户不存在、密码错误等） */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /** 无权限 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponse.error(403, "无访问权限");
    }

    /** 未预期异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
