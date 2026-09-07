package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer status;
    private Integer onlineStatus;
    private LocalDateTime firstLoginTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastVisitTime;
    private LocalDateTime lastActiveTime;
    private Integer activityScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
