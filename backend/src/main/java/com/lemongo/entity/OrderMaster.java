package com.lemongo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_master")
public class OrderMaster {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String payStatus;
    private String paymentMethod;
    private LocalDateTime paidTime;
    private LocalDateTime cancelledTime;
    private LocalDateTime finishedTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
