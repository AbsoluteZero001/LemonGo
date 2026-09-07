package com.lemongo.vo;

import java.time.LocalDateTime;

public record ProfileVo(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatarUrl,
        LocalDateTime lastLoginTime,
        LocalDateTime lastActiveTime,
        Integer activityScore) {
}
