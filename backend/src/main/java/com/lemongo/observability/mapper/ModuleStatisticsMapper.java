package com.lemongo.observability.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemongo.entity.ModuleStatistics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ModuleStatisticsMapper extends BaseMapper<ModuleStatistics> {

    @Insert("""
            INSERT INTO module_statistics (
                stat_date, module_id, module_name,
                request_count, success_count, error_count,
                total_duration_ms, max_duration_ms, created_at, updated_at
            ) VALUES (
                CURDATE(), #{moduleId}, #{moduleName},
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
    void addModuleStat(
            @Param("moduleId") Long moduleId,
            @Param("moduleName") String moduleName,
            @Param("success") int success,
            @Param("error") int error,
            @Param("durationMs") long durationMs);
}
