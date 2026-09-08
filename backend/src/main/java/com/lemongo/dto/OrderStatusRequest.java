package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequest(
        @NotBlank(message = "订单状态不能为空") String orderStatus,
        @NotBlank(message = "支付状态不能为空") String payStatus) {
}
