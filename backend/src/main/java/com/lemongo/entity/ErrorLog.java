package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("error_log")
public class ErrorLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestId;
    private Long apiId;
    private Long moduleId;
    private Long developerId;
    private Integer errorCode;
    private String errorType;
    private String errorMessage;
    private String exceptionClass;
    private String stackTrace;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
