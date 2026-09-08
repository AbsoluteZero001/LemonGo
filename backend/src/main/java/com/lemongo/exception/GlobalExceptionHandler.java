package com.lemongo.exception;

import com.lemongo.common.api.Result;
import com.lemongo.common.api.ResultCode;
import com.lemongo.observability.ErrorTraceResolver;
import com.lemongo.observability.ErrorTraceVo;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorTraceResolver errorTraceResolver;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getCode());
        return build(status == null ? HttpStatus.BAD_REQUEST : status, ex.getMessage(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = firstFieldError(ex);
        return build(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException ex) {
        return build(HttpStatus.BAD_REQUEST, firstFieldError(ex), ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "请求体格式不正确", ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "请求的资源不存在", ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "不支持的 HTTP 方法", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ResultCode.INTERNAL_ERROR.getDefaultMessage(), ex);
    }

    private ResponseEntity<Result<Void>> build(
            HttpStatus status,
            String message,
            Throwable ex) {
        ErrorTraceVo trace = errorTraceResolver.resolve(ex, status.value());
        if (status.value() >= 500) {
            log.error("request failed requestId={} path={} status={}",
                    trace.requestId(), trace.path(), status.value(), ex);
        } else {
            log.warn("request failed requestId={} path={} status={} message={}",
                    trace.requestId(), trace.path(), status.value(), message);
        }
        return ResponseEntity.status(status)
                .body(Result.fail(status.value(), message));
    }

    private String firstFieldError(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        return Objects.requireNonNullElse(fieldError, ex.getBindingResult().getGlobalError())
                .getDefaultMessage();
    }
}
