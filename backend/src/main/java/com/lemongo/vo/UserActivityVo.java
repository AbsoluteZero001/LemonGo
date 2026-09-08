package com.lemongo.vo;

import java.time.LocalDateTime;

public record UserActivityVo(
        Long userId,
        String username,
        String nickname,
        LocalDateTime lastLoginTime,
        LocalDateTime lastLogoutTime,
        LocalDateTime lastActiveTime,
        int todayVisits,
        int totalVisits,
        int activeSecondsTotal,
        int activityScore,
        int onlineStatus) {
}
