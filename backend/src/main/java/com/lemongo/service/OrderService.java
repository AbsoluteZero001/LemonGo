package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.dto.OrderCreateRequest;
import com.lemongo.entity.CartItem;
import com.lemongo.entity.OrderItem;
import com.lemongo.entity.OrderMaster;
import com.lemongo.entity.Product;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.CartItemMapper;
import com.lemongo.mapper.OrderItemMapper;
import com.lemongo.mapper.OrderMasterMapper;
import com.lemongo.mapper.ProductMapper;
import com.lemongo.vo.OrderCreateVo;
import com.lemongo.vo.OrderDetailVo;
import com.lemongo.vo.OrderItemVo;
import com.lemongo.vo.OrderSummaryVo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMasterMapper orderMasterMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Transactional
    public OrderCreateVo createOrder(OrderCreateRequest request) {
        Long userId = currentUserId();
        List<CartItem> cartItems = cartItemsByIds(userId, request.cartItemIds());
        Map<Long, Product> products = productMapper.selectBatchIds(
                cartItems.stream().map(CartItem::getProductId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = products.get(cartItem.getProductId());
            if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
                throw new BusinessException(400, "商品已下架：" + cartItem.getProductId());
            }
            int changed = productMapper.reduceStock(product.getId(), cartItem.getQuantity());
            if (changed == 0) {
                throw new BusinessException(400,
                        "库存不足：" + product.getProductName() + " 剩余 " + product.getStock());
            }
            BigDecimal subtotal = product.getPrice().multiply(
                    BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImageUrl(product.getImageUrl());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(subtotal);
            orderItem.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
            orderItems.add(orderItem);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        OrderMaster order = new OrderMaster();
        order.setOrderNo(nextOrderNo(now));
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setOrderStatus("CREATED");
        order.setPayStatus("UNPAID");
        order.setRemark(request.remark());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderMasterMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }
        cartItemMapper.deleteBatchIds(cartItems.stream().map(CartItem::getId).toList());
        return new OrderCreateVo(order.getId(), order.getOrderNo(), order.getTotalAmount());
    }

    public Page<OrderSummaryVo> listOrders(long page, long size) {
        Page<OrderMaster> orderPage = orderMasterMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<OrderMaster>()
                        .eq(OrderMaster::getUserId, currentUserId())
                        .orderByDesc(OrderMaster::getCreatedAt));
        Page<OrderSummaryVo> result = new Page<>(orderPage.getCurrent(), orderPage.getSize());
        result.setTotal(orderPage.getTotal());
        result.setRecords(orderPage.getRecords().stream()
                .map(order -> new OrderSummaryVo(
                        order.getId(),
                        order.getOrderNo(),
                        order.getTotalAmount(),
                        order.getOrderStatus(),
                        order.getPayStatus(),
                        order.getPaymentMethod(),
                        order.getCreatedAt()))
                .toList());
        return result;
    }

    public OrderDetailVo orderDetail(Long id) {
        OrderMaster order = ownedOrder(id);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        List<OrderItemVo> itemVos = items.stream()
                .map(item -> new OrderItemVo(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()))
                .toList();
        return new OrderDetailVo(
                order.getId(),
                order.getOrderNo(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getPaymentMethod(),
                order.getPaidTime(),
                order.getCreatedAt(),
                order.getRemark(),
                itemVos);
    }

    @Transactional
    public OrderDetailVo pay(Long id) {
        OrderMaster order = ownedOrder(id);
        if ("PAID".equals(order.getPayStatus())) {
            return orderDetail(id);
        }
        order.setPayStatus("PAID");
        order.setOrderStatus("PAID");
        order.setPaymentMethod("WALLET");
        order.setPaidTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        order.setUpdatedAt(order.getPaidTime());
        orderMasterMapper.updateById(order);
        return orderDetail(id);
    }

    private OrderMaster ownedOrder(Long id) {
        OrderMaster order = orderMasterMapper.selectById(id);
        if (order == null || !order.getUserId().equals(currentUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private List<CartItem> cartItemsByIds(Long userId, List<Long> ids) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, ids));
        if (items.isEmpty()) {
            throw new BusinessException(400, "没有可结算的商品");
        }
        return items;
    }

    private Long currentUserId() {
        Long userId = RequestContext.userId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return userId;
    }

    private String nextOrderNo(LocalDateTime now) {
        String random = String.valueOf((int) (Math.random() * 9000) + 1000);
        return "LG" + now.format(ORDER_TIME) + random;
    }
}
