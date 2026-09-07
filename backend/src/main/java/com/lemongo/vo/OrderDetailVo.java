package com.lemongo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailVo(
        Long id,
        String orderNo,
        BigDecimal totalAmount,
        String orderStatus,
        String payStatus,
        String paymentMethod,
        LocalDateTime paidTime,
        LocalDateTime createdAt,
        String remark,
        List<OrderItemVo> items) {
}
