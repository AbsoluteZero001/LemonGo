package com.lemongo.observability;

import java.time.LocalDateTime;

public record ErrorTraceVo(
        int code,
        String message,
        String errorType,
        String requestId,
        String path,
        String method,
        String controller,
        String controllerMethod,
        String service,
        String mapper,
        String module,
        String moduleCode,
        Owner owner,
        LocalDateTime timestamp) {

    public record Owner(Long id, String name, String employeeNo, String department) {
    }
}
