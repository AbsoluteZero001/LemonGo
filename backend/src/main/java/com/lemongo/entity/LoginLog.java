package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("login_log")
public class LoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime loginTime;
    private String loginIp;
    private String userAgent;
    private Integer loginStatus;
    private String failReason;
    private LocalDateTime lastActiveTime;
    private LocalDateTime logoutTime;
    private Integer activeSeconds;
    private Integer sessionStatus;
    private LocalDateTime createdAt;
}
