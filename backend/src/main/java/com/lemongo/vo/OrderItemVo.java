package com.lemongo.vo;

import java.math.BigDecimal;

public record OrderItemVo(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal) {
}
