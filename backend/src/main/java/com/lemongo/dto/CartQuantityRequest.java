package com.lemongo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartQuantityRequest(
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        Integer quantity) {
}
