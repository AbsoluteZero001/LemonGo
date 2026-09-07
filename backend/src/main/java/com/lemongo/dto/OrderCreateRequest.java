package com.lemongo.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
        @NotEmpty(message = "请选择要结算的商品") List<Long> cartItemIds,
        String remark) {
}
