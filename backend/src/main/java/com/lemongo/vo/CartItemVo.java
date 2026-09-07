package com.lemongo.vo;

import java.math.BigDecimal;

public record CartItemVo(
        Long id,
        Long productId,
        String productName,
        String category,
        BigDecimal price,
        String imageUrl,
        Integer quantity,
        Integer stock,
        Integer checked) {
}
