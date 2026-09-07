package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_api")
public class SysApi {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String apiPath;
    private String httpMethod;
    private Long moduleId;
    private String controllerName;
    private String controllerMethod;
    private String serviceName;
    private String mapperName;
    private String description;
    private Long developerId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
