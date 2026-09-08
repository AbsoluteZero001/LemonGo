package com.lemongo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderAdminVo(
        Long id,
        String orderNo,
        Long userId,
        String username,
        BigDecimal totalAmount,
        String orderStatus,
        String payStatus,
        String paymentMethod,
        LocalDateTime paidTime,
        LocalDateTime cancelledTime,
        LocalDateTime finishedTime,
        String remark,
        LocalDateTime createdAt) {
}
