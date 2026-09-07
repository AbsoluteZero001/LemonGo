package com.lemongo.vo;

import java.time.LocalDateTime;

public record ModuleMonitorVo(
        Long moduleId,
        String moduleName,
        String moduleCode,
        String description,
        Long developerId,
        String developerName,
        String employeeNo,
        long requestCount,
        long successCount,
        long errorCount,
        long avgDurationMs,
        LocalDateTime lastActiveTime) {
}
