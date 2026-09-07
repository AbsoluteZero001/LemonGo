package com.lemongo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductSaveRequest(
        @NotBlank(message = "商品名称不能为空") String productName,
        @NotBlank(message = "分类不能为空") String category,
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        BigDecimal price,
        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能小于 0")
        Integer stock,
        String imageUrl,
        String detailText) {
}
