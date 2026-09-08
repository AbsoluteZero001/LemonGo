package com.lemongo.observability.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.UserActivity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserActivityMapper extends BaseMapper<UserActivity> {

    @Insert("""
            INSERT INTO user_activity (
                user_id, stat_date, first_login_time, last_login_time, last_active_time,
                request_count_today, request_count_total, active_seconds_today,
                active_seconds_total, activity_score, online_status,
                created_at, updated_at
            ) VALUES (
                #{userId}, CURDATE(), NOW(), NOW(), NOW(),
                1, 1, #{durationMs}, #{durationMs}, 1, 1,
                NOW(), NOW()
            )
            ON DUPLICATE KEY UPDATE
                first_login_time = COALESCE(first_login_time, VALUES(first_login_time)),
                last_login_time = NOW(),
                last_active_time = NOW(),
                request_count_today = request_count_today + 1,
                request_count_total = request_count_total + 1,
                active_seconds_today = active_seconds_today
                    + LEAST(60, GREATEST(1, #{durationMs} / 1000)),
                active_seconds_total = active_seconds_total
                    + LEAST(60, GREATEST(1, #{durationMs} / 1000)),
                activity_score = LEAST(100, activity_score + 1),
                online_status = 1,
                updated_at = NOW()
            """)
    void addActivity(@Param("userId") Long userId, @Param("durationMs") long durationMs);

    @Select("""
            SELECT user_id AS userId,
                   COALESCE(SUM(request_count_today), 0) AS requestCount,
                   COALESCE(SUM(active_seconds_today), 0) AS activeSeconds
            FROM user_activity
            WHERE stat_date <= #{statDate}
            GROUP BY user_id
            """)
    List<Map<String, Object>> selectCumulative(@Param("statDate") LocalDate statDate);

    @Select("""
            SELECT user_id AS userId,
                   COALESCE(SUM(request_count_today), 0) AS requestCount,
                   COALESCE(SUM(active_seconds_today), 0) AS activeSeconds
            FROM user_activity
            WHERE user_id = #{userId} AND stat_date <= #{statDate}
            GROUP BY user_id
            """)
    Map<String, Object> selectUserCumulative(
            @Param("userId") Long userId,
            @Param("statDate") LocalDate statDate);
}
