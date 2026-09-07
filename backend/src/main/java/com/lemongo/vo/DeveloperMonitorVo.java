package com.lemongo.vo;

public record DeveloperMonitorVo(
        Long developerId,
        String name,
        String employeeNo,
        String department,
        long requestCount,
        long errorCount,
        long totalDurationMs,
        double errorRate) {
}
