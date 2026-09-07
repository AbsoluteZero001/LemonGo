package com.lemongo.vo;

import java.util.List;

public record DashboardVo(
        long totalRequests,
        long todayRequests,
        long todayErrors,
        long onlineUsers,
        List<TrendPoint> trend,
        List<ModuleMetric> moduleMetrics,
        List<ApiMetric> apiMetrics) {

    public record TrendPoint(String date, long requestCount, long errorCount) {
    }

    public record ModuleMetric(
            Long moduleId,
            String moduleName,
            Long developerId,
            String developerName,
            long requestCount,
            long errorCount,
            long avgDurationMs) {
    }

    public record ApiMetric(
            Long apiId,
            String apiPath,
            String httpMethod,
            long requestCount,
            long errorCount,
            long avgDurationMs) {
    }
}
