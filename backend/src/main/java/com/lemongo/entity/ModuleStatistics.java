package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("module_statistics")
public class ModuleStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long moduleId;
    private String moduleName;
    private Integer requestCount;
    private Integer successCount;
    private Integer errorCount;
    private Long totalDurationMs;
    private Integer maxDurationMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
