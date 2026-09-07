package com.lemongo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryVo(
        Long id,
        String orderNo,
        BigDecimal totalAmount,
        String orderStatus,
        String payStatus,
        String paymentMethod,
        LocalDateTime createdAt) {
}
