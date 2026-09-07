package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("request_log")
public class RequestLog {
    @TableId
    private String requestId;
    private Long userId;
    private String username;
    private LocalDateTime requestTime;
    private String clientIp;
    private String httpMethod;
    private String uri;
    private String paramSummary;
    private String controllerName;
    private String controllerMethod;
    private String serviceName;
    private String mapperName;
    private Long moduleId;
    private String moduleName;
    private Long developerId;
    private String developerName;
    private Integer httpStatus;
    private Integer success;
    private String errorType;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private LocalDateTime createdAt;
}
