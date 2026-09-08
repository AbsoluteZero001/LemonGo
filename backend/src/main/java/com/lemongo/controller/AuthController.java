package com.lemongo.controller;

import com.lemongo.common.api.Result;
import com.lemongo.dto.LoginRequest;
import com.lemongo.dto.RegisterRequest;
import com.lemongo.service.AuthService;
import com.lemongo.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public Result<LoginVo> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return Result.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/auth/register")
    public Result<LoginVo> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return Result.ok(authService.register(request, httpRequest));
    }

    @PostMapping("/auth/heartbeat")
    public Result<Void> heartbeat() {
        authService.heartbeat();
        return Result.ok();
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }
}
