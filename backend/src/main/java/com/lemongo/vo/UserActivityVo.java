package com.lemongo.vo;

import java.time.LocalDateTime;

public record UserActivityVo(
        Long userId,
        String username,
        String nickname,
        LocalDateTime lastLoginTime,
        LocalDateTime lastActiveTime,
        LocalDateTime lastVisitTime,
        int requestCountToday,
        int requestCountTotal,
        int activeSecondsToday,
        int activeSecondsTotal,
        int activityScore,
        int onlineStatus) {
}
