package com.lemongo.controller;

import com.lemongo.common.api.Result;
import com.lemongo.dto.CartAddRequest;
import com.lemongo.dto.CartQuantityRequest;
import com.lemongo.service.CartService;
import com.lemongo.vo.CartItemVo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Result<List<CartItemVo>> list() {
        return Result.ok(cartService.listItems());
    }

    @PostMapping("/items")
    public Result<CartItemVo> add(@Valid @RequestBody CartAddRequest request) {
        return Result.ok(cartService.add(request));
    }

    @PutMapping("/items/{id}")
    public Result<Void> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CartQuantityRequest request) {
        cartService.updateQuantity(id, request.quantity());
        return Result.ok();
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return Result.ok();
    }
}
