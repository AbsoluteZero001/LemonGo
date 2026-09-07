package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.dto.CartAddRequest;
import com.lemongo.entity.CartItem;
import com.lemongo.entity.Product;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.CartItemMapper;
import com.lemongo.vo.CartItemVo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductService productService;

    public List<CartItemVo> listItems() {
        Long userId = currentUserId();
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreatedAt));
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();
        Map<Long, Product> products = productService.listByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<CartItemVo> result = new ArrayList<>();
        for (CartItem item : items) {
            Product product = products.get(item.getProductId());
            if (product == null) {
                continue;
            }
            result.add(new CartItemVo(
                    item.getId(),
                    product.getId(),
                    product.getProductName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getImageUrl(),
                    item.getQuantity(),
                    product.getStock(),
                    item.getChecked()));
        }
        result.sort(Comparator.comparing(CartItemVo::id).reversed());
        return result;
    }

    @Transactional
    public CartItemVo add(CartAddRequest request) {
        Long userId = currentUserId();
        Product product = productService.detail(request.productId());
        int quantity = request.quantity() == null ? 1 : request.quantity();
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, product.getId()));
        if (existing != null) {
            int nextQuantity = existing.getQuantity() + quantity;
            if (nextQuantity > product.getStock()) {
                throw new BusinessException(400, "库存不足，最多可加 " + product.getStock() + " 件");
            }
            existing.setQuantity(nextQuantity);
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
            cartItemMapper.updateById(existing);
            return toVo(existing, product);
        }
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(quantity);
        cartItem.setChecked(1);
        cartItem.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        cartItem.setUpdatedAt(cartItem.getCreatedAt());
        cartItemMapper.insert(cartItem);
        return toVo(cartItem, product);
    }

    @Transactional
    public void updateQuantity(Long itemId, Integer quantity) {
        Long userId = currentUserId();
        CartItem item = ownedItem(itemId, userId);
        Product product = productService.detail(item.getProductId());
        if (quantity > product.getStock()) {
            throw new BusinessException(400, "库存不足，最多可加 " + product.getStock() + " 件");
        }
        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        cartItemMapper.updateById(item);
    }

    @Transactional
    public void remove(Long itemId) {
        CartItem item = ownedItem(itemId, currentUserId());
        cartItemMapper.deleteById(item.getId());
    }

    public List<CartItem> ownedCartItems(Long userId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, itemIds));
        if (items.size() != itemIds.stream().distinct().count()) {
            throw new BusinessException(400, "部分购物车商品不存在");
        }
        return items;
    }

    public void deleteItems(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            cartItemMapper.deleteBatchIds(ids);
        }
    }

    private CartItem ownedItem(Long itemId, Long userId) {
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车商品不存在");
        }
        return item;
    }

    private Long currentUserId() {
        Long userId = RequestContext.userId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        return userId;
    }

    private CartItemVo toVo(CartItem item, Product product) {
        return new CartItemVo(
                item.getId(),
                product.getId(),
                product.getProductName(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                item.getQuantity(),
                product.getStock(),
                item.getChecked());
    }
}
