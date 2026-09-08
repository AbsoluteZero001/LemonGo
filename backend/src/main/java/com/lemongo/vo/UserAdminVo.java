package com.lemongo.vo;

import java.time.LocalDateTime;

public record UserAdminVo(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatarUrl,
        Integer status,
        String role,
        Integer onlineStatus,
        LocalDateTime firstLoginTime,
        LocalDateTime lastLoginTime,
        LocalDateTime lastActiveTime,
        Integer activityScore,
        LocalDateTime createdAt) {
}
