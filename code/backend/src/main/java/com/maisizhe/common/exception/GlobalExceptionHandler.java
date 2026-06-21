package com.maisizhe.common.exception;

import com.maisizhe.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，记录详细日志
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 业务异常 [code={}]: {}", requestId, e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理群组权限异常
     */
    @ExceptionHandler(GroupPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleGroupPermissionException(GroupPermissionException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 权限异常: {}", requestId, e.getMessage());
        return Result.forbidden(e.getMessage());
    }
    
    /**
     * 处理访问拒绝异常（Spring Security）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 访问被拒绝: {} | URI: {}", requestId, e.getMessage(), request.getRequestURI());
        return Result.forbidden("无权限访问该资源");
    }
    
    /**
     * 处理参数校验异常(@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[{}] 参数校验失败: {}", requestId, errors);
        return Result.error(400, "参数校验失败: " + errors);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[{}] 参数绑定失败: {}", requestId, errors);
        return Result.error(400, "参数绑定失败: " + errors);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        String paramName = e.getName();
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知";
        log.warn("[{}] 参数类型不匹配: {} 应为 {}", requestId, paramName, expectedType);
        return Result.error(400, "参数 " + paramName + " 类型错误，应为 " + expectedType);
    }
    
    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 请求体解析失败: {}", requestId, e.getMessage());
        return Result.error(400, "请求体格式错误，请检查JSON格式");
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 非法参数: {}", requestId, e.getMessage());
        return Result.error(400, e.getMessage());
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.warn("[{}] 路径不存在: {}", requestId, e.getRequestURL());
        return Result.error(404, "请求路径不存在: " + e.getRequestURL());
    }
    
    /**
     * 处理数据库异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSQLException(SQLException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.error("[{}] 数据库异常 [SQLState={}]: {}", requestId, e.getSQLState(), e.getMessage(), e);
        return Result.error(500, "数据库操作失败，请稍后重试");
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        log.error("[{}] 空指针异常: {}", requestId, e.getMessage(), e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        String uri = request.getRequestURI();
        String method = request.getMethod();
        log.error("[{}] 系统异常 [{} {}]: {}", requestId, method, uri, e.getMessage(), e);
        return Result.error(500, "系统繁忙，请稍后重试 [Request-Id: " + requestId + "]");
    }
}
