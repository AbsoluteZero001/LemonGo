package com.lemongo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
        @NotNull(message = "商品不能为空") Long productId,
        @Min(value = 1, message = "数量至少为 1") Integer quantity) {
}
