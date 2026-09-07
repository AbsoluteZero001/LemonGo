package com.lemongo.controller;

import com.lemongo.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统基础")
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<HealthInfo> health() {
        return Result.ok(new HealthInfo(
                "lemon-go",
                "UP",
                LocalDateTime.now().format(TIMESTAMP)));
    }

    public record HealthInfo(String serviceName, String status, String timestamp) {
    }
}

