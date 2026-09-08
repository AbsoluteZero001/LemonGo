package com.lemongo.controller;

import com.lemongo.common.api.Result;
import com.lemongo.dto.ProfileUpdateRequest;
import com.lemongo.service.UserService;
import com.lemongo.vo.ProfileVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public Result<ProfileVo> me() {
        return Result.ok(userService.me());
    }

    @PutMapping("/users/me")
    public Result<ProfileVo> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {
        return Result.ok(userService.updateMe(request));
    }
}
