package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.OrderCreateRequest;
import com.lemongo.service.OrderService;
import com.lemongo.vo.OrderCreateVo;
import com.lemongo.vo.OrderDetailVo;
import com.lemongo.vo.OrderSummaryVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderCreateVo> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.createOrder(request));
    }

    @GetMapping
    public Result<PageResult<OrderSummaryVo>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return Result.ok(PageResult.of(orderService.listOrders(page, size)));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVo> detail(@PathVariable Long id) {
        return Result.ok(orderService.orderDetail(id));
    }

    @PostMapping("/{id}/pay")
    public Result<OrderDetailVo> pay(@PathVariable Long id) {
        return Result.ok(orderService.pay(id));
    }
}
