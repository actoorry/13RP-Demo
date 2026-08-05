package com.boyu.demo.controller;

import com.boyu.demo.orchestrator.DemoStateMachine;
import com.boyu.demo.orchestrator.TimelineController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局 REST 异常处理兜底。
 * <p>统一返回 {@code {ok:false, phase, progress, message, error}}，与业务控制器
 * {@code error()} 格式完全一致；业务控制器内已 try-catch 的场景不会走到这里，
 * 此处理器覆盖遗漏的异常路径。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final DemoStateMachine stateMachine;
    private final TimelineController timeline;

    public GlobalExceptionHandler(DemoStateMachine stateMachine, TimelineController timeline) {
        this.stateMachine = stateMachine;
        this.timeline = timeline;
    }

    /** 非法状态迁移等业务冲突 → 400。 */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalState(IllegalStateException e) {
        return error(e.getMessage());
    }

    /** 权限不足 → 403（管理端权限码校验被拒时不应落入 500 兜底）。 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException e) {
        log.warn("AccessDenied: {}", e.getMessage());
        return error("无操作权限");
    }

    /** 未知异常兜底 → 500，不向客户端泄露内部细节；必须打印日志便于联调排查。 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        log.error("未处理异常（GlobalExceptionHandler 兜底）", e);
        return error("系统错误");
    }

    /** 与 {@code DemoController#error} 同构：ok=false 时补充 phase/progress/message 快照。 */
    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", false);
        m.put("phase", stateMachine.getPhase().name());
        m.put("progress", timeline.getProgress());
        m.put("message", stateMachine.getLastMessage());
        m.put("error", msg);
        return m;
    }
}
