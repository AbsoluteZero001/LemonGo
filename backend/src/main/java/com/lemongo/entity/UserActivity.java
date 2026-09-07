package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_activity")
public class UserActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate statDate;
    private LocalDateTime firstLoginTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastActiveTime;
    private Integer requestCountToday;
    private Integer requestCountTotal;
    private Integer activeSecondsToday;
    private Integer activeSecondsTotal;
    private Integer activityScore;
    private Integer onlineStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
