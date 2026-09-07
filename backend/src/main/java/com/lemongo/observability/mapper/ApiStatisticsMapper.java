package com.lemongo.observability.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.ApiStatistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ApiStatisticsMapper extends BaseMapper<ApiStatistics> {

    @Insert("""
            INSERT INTO api_statistics (
                stat_date, api_id, api_path, http_method,
                request_count, success_count, error_count,
                total_duration_ms, max_duration_ms, created_at, updated_at
            ) VALUES (
                CURDATE(), #{apiId}, #{apiPath}, #{httpMethod},
                1, #{success}, #{error}, #{durationMs}, #{durationMs}, NOW(), NOW()
            )
            ON DUPLICATE KEY UPDATE
                request_count = request_count + 1,
                success_count = success_count + #{success},
                error_count = error_count + #{error},
                total_duration_ms = total_duration_ms + #{durationMs},
                max_duration_ms = GREATEST(max_duration_ms, #{durationMs}),
                updated_at = NOW()
            """)
    void addApiStat(
            @Param("apiId") Long apiId,
            @Param("apiPath") String apiPath,
            @Param("httpMethod") String httpMethod,
            @Param("success") int success,
            @Param("error") int error,
            @Param("durationMs") long durationMs);
}
