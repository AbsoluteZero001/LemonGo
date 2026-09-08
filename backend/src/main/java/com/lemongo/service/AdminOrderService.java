package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.OrderStatusRequest;
import com.lemongo.entity.OrderMaster;
import com.lemongo.entity.SysUser;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.OrderMasterMapper;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.vo.OrderAdminVo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final OrderMasterMapper orderMasterMapper;
    private final SysUserMapper userMapper;

    public Page<OrderAdminVo> list(
            long page,
            long size,
            String keyword,
            String orderStatus,
            String payStatus) {
        Page<OrderMaster> orderPage = orderMasterMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<OrderMaster>()
                        .like(StringUtils.hasText(keyword), OrderMaster::getOrderNo, keyword)
                        .eq(StringUtils.hasText(orderStatus), OrderMaster::getOrderStatus, orderStatus)
                        .eq(StringUtils.hasText(payStatus), OrderMaster::getPayStatus, payStatus)
                        .orderByDesc(OrderMaster::getCreatedAt));
        Map<Long, String> usernames = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        Page<OrderAdminVo> result = new Page<>(orderPage.getCurrent(), orderPage.getSize());
        result.setTotal(orderPage.getTotal());
        result.setRecords(orderPage.getRecords().stream()
                .map(order -> toVo(order, usernames.get(order.getUserId())))
                .toList());
        return result;
    }

    public OrderAdminVo updateStatus(Long id, OrderStatusRequest request) {
        OrderMaster order = require(id);
        LocalDateTime now = LocalDateTime.now(ZONE);
        order.setOrderStatus(request.orderStatus());
        order.setPayStatus(request.payStatus());
        if ("PAID".equals(request.orderStatus())) {
            order.setPaidTime(now);
        }
        if ("CANCELLED".equals(request.orderStatus())) {
            order.setCancelledTime(now);
        }
        if ("FINISHED".equals(request.orderStatus())) {
            order.setFinishedTime(now);
        }
        order.setUpdatedAt(now);
        orderMasterMapper.updateById(order);
        SysUser user = userMapper.selectById(order.getUserId());
        return toVo(order, user == null ? null : user.getUsername());
    }

    private OrderMaster require(Long id) {
        OrderMaster order = orderMasterMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private OrderAdminVo toVo(OrderMaster order, String username) {
        return new OrderAdminVo(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                username,
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getPaymentMethod(),
                order.getPaidTime(),
                order.getCancelledTime(),
                order.getFinishedTime(),
                order.getRemark(),
                order.getCreatedAt());
    }
}
