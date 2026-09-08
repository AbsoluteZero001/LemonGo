package com.lemongo.observability.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.LoginLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface LoginLogMapper extends BaseMapper<LoginLog> {

    @Update("""
            UPDATE login_log
            SET active_seconds = active_seconds + CASE
                    WHEN TIMESTAMPDIFF(SECOND, last_active_time, #{now}) BETWEEN 1 AND 300
                        THEN TIMESTAMPDIFF(SECOND, last_active_time, #{now})
                    ELSE 0
                END,
                last_active_time = #{now}
            WHERE user_id = #{userId}
              AND login_status = 1
              AND session_status = 1
              AND last_active_time IS NOT NULL
            """)
    int touchOpenSession(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Update("""
            UPDATE login_log
            SET active_seconds = active_seconds + CASE
                    WHEN TIMESTAMPDIFF(SECOND, last_active_time, #{now}) BETWEEN 1 AND 300
                        THEN TIMESTAMPDIFF(SECOND, last_active_time, #{now})
                    ELSE 0
                END,
                last_active_time = #{now},
                logout_time = #{now},
                session_status = 0
            WHERE user_id = #{userId}
              AND login_status = 1
              AND session_status = 1
              AND last_active_time IS NOT NULL
            """)
    int closeOpenSession(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Update("""
            UPDATE login_log
            SET active_seconds = active_seconds + CASE
                    WHEN TIMESTAMPDIFF(SECOND, last_active_time, #{now}) BETWEEN 1 AND 300
                        THEN TIMESTAMPDIFF(SECOND, last_active_time, #{now})
                    ELSE 0
                END,
                last_active_time = #{now},
                session_status = 0
            WHERE user_id = #{userId}
              AND login_status = 1
              AND session_status = 1
              AND last_active_time IS NOT NULL
            """)
    int closeStaleSession(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Update("""
            UPDATE login_log ll
            JOIN (
                SELECT MAX(id) AS id
                FROM login_log
                WHERE user_id = #{userId} AND login_status = 1
            ) latest ON latest.id = ll.id
            SET ll.last_active_time = #{now},
                ll.logout_time = NULL,
                ll.session_status = 1
            WHERE ll.session_status = 0
              AND ll.logout_time IS NULL
            """)
    int openLatestSession(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Select("""
            SELECT user_id AS userId,
                   COUNT(*) AS visitCount
            FROM login_log
            WHERE login_status = 1 AND user_id IS NOT NULL
            GROUP BY user_id
            """)
    List<Map<String, Object>> selectTotalVisits();

    @Select("""
            SELECT user_id AS userId,
                   COUNT(*) AS visitCount
            FROM login_log
            WHERE login_status = 1
              AND user_id IS NOT NULL
              AND login_time >= #{startTime}
              AND login_time < #{endTime}
            GROUP BY user_id
            """)
    List<Map<String, Object>> selectDailyVisits(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT user_id AS userId,
                   COALESCE(SUM(
                       active_seconds + IF(
                           session_status = 1
                           AND last_active_time IS NOT NULL
                           AND TIMESTAMPDIFF(SECOND, last_active_time, NOW()) BETWEEN 0 AND 300,
                           TIMESTAMPDIFF(SECOND, last_active_time, NOW()),
                           0
                       )
                   ), 0) AS activeSeconds
            FROM login_log
            WHERE login_status = 1 AND user_id IS NOT NULL
            GROUP BY user_id
            """)
    List<Map<String, Object>> selectActiveSeconds();

    @Select("""
            SELECT user_id AS userId,
                   MAX(logout_time) AS lastLogoutTime
            FROM login_log
            WHERE login_status = 1
              AND user_id IS NOT NULL
              AND logout_time IS NOT NULL
            GROUP BY user_id
            """)
    List<Map<String, Object>> selectLastLogout();
}
