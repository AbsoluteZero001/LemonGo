package com.lemongo.controller;

import com.lemongo.common.api.Result;
import com.lemongo.service.UserService;
import com.lemongo.vo.ProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
