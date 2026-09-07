package com.lemongo.vo;

import java.math.BigDecimal;

public record OrderCreateVo(Long orderId, String orderNo, BigDecimal totalAmount) {
}
