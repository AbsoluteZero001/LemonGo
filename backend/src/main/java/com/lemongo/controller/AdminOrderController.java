package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.OrderStatusRequest;
import com.lemongo.service.AdminOrderService;
import com.lemongo.vo.OrderAdminVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public Result<PageResult<OrderAdminVo>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String payStatus) {
        return Result.ok(PageResult.of(
                adminOrderService.list(page, size, keyword, orderStatus, payStatus)));
    }

    @PutMapping("/{id}/status")
    public Result<OrderAdminVo> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody OrderStatusRequest request) {
        return Result.ok(adminOrderService.updateStatus(id, request));
    }
}
