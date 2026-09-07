package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_developer")
public class SysDeveloper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String employeeNo;
    private String email;
    private String department;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
