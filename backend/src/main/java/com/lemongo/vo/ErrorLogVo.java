package com.lemongo.vo;

import java.time.LocalDateTime;

public record ErrorLogVo(
        Long id,
        String requestId,
        Long apiId,
        Long moduleId,
        String moduleName,
        Long developerId,
        String developerName,
        String username,
        Integer errorCode,
        String errorType,
        String errorMessage,
        String exceptionClass,
        String stackTrace,
        LocalDateTime occurredAt) {
}
