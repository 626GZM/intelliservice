package com.intelliservice.backend.common;

import com.intelliservice.backend.model.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 明确的业务异常消息（可直接展示给用户） */
    private static final Set<String> SAFE_MESSAGES = Set.of(
            "用户名已存在", "用户不存在", "密码错误",
            "文档不存在", "商品不存在", "商户不存在",
            "工单不存在", "会话不存在",
            "该会话已提交过评价", "该会话已有客服接入", "该会话不在等待状态",
            "工单尚未通过审阅，无法执行处罚", "工单未关联商户，无法执行处罚",
            "工单没有处罚方案", "处罚方案格式错误，执行失败",
            "缺少 sessionId 参数", "缺少参数",
            "Agent 配置不存在",
            "系统繁忙，请稍后重试", "AI 服务暂时不可用，请稍后重试"
    );

    /** 业务异常 */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        String msg = e.getMessage();
        if (msg != null && (SAFE_MESSAGES.contains(msg)
                || msg.startsWith("不支持的文件类型")
                || msg.startsWith("Agent 名称已存在")
                || msg.startsWith("Agent 配置不存在: ")
                || msg.startsWith("工具名称已存在")
                || msg.equals("工具配置不存在")
                || msg.startsWith("订单不存在")
                || msg.startsWith("判罚规则不存在"))) {
            log.warn("业务异常: {}", msg);
            return ApiResponse.error(400, msg);
        }
        log.error("未预期 RuntimeException", e);
        return ApiResponse.error(400, "操作失败，请稍后重试");
    }

    /** 上传文件超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ApiResponse.error(400, "文件大小超出限制");
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
