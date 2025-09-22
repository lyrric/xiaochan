package io.github.xiaochan.config;

import io.github.xiaochan.model.BaseResult;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@ResponseBody
@Priority(1)
public class GlobalResultExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResult<?> handleBusinessException(HttpServletResponse response, BusinessException e) {
        setResponseStatus(response);
        log.warn(e.getMessage(), e);
        return buildError(e.getMessage());
    }
    
    /**
     * 处理@Valid注解校验失败异常（用于@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<?> handleMethodArgumentNotValidException(HttpServletResponse response, MethodArgumentNotValidException e) {
        setResponseStatus(response);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMessage);
        return buildError(errorMessage);
    }
    
    /**
     * 处理@Valid注解校验失败异常（用于@RequestParam、@PathVariable等）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResult<?> handleConstraintViolationException(HttpServletResponse response, ConstraintViolationException e) {
        setResponseStatus(response);
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMessage);
        return buildError(errorMessage);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public BaseResult<?> handleBindException(HttpServletResponse response, BindException e) {
        setResponseStatus(response);
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", errorMessage);
        return buildError(errorMessage);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public BaseResult<?> handleNoResourceFoundException(HttpServletResponse response, NoResourceFoundException e) {
        setResponseStatus(response);
        return buildError("url路径不存在");
    }

    @ExceptionHandler(Exception.class)
    public BaseResult<?> handleException(HttpServletResponse response, Exception e) {
        setResponseStatus(response);
        log.error(e.getMessage(), e);
        return buildError(null);
    }
    protected void setResponseStatus(HttpServletResponse response){
        response.setStatus(HttpStatus.OK.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    protected BaseResult<?> buildError(String msg) {
        if (StringUtils.isNoneBlank(msg)) {
            return BaseResult.error(msg);
        }
        return BaseResult.error("服务器异常");
    }

}