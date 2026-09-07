package com.lemongo.observability.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.RequestLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RequestLogMapper extends BaseMapper<RequestLog> {

    @Select("""
            SELECT DATE_FORMAT(request_time, '%Y-%m-%d') AS statDate,
                   COUNT(*) AS requestCount,
                   COALESCE(SUM(http_status >= 400), 0) AS errorCount
            FROM request_log
            WHERE request_time >= #{startTime}
            GROUP BY DATE_FORMAT(request_time, '%Y-%m-%d')
            ORDER BY statDate
            """)
    List<Map<String, Object>> selectTrend(@Param("startTime") LocalDateTime startTime);

    @Select("""
            SELECT developer_id AS developerId,
                   developer_name AS developerName,
                   COUNT(*) AS requestCount,
                   COALESCE(SUM(http_status >= 400), 0) AS errorCount,
                   COALESCE(SUM(duration_ms), 0) AS totalDurationMs
            FROM request_log
            WHERE request_time >= #{startTime}
            GROUP BY developer_id, developer_name
            ORDER BY requestCount DESC
            """)
    List<Map<String, Object>> selectDeveloperStats(
            @Param("startTime") LocalDateTime startTime);
}
