package com.lemongo.vo;

import java.time.LocalDateTime;

public record UserRequestUsageVo(
        Long userId,
        String username,
        String httpMethod,
        String uri,
        String moduleName,
        String developerName,
        long requestCount,
        long errorCount,
        long totalDurationMs,
        LocalDateTime lastRequestTime) {
}
