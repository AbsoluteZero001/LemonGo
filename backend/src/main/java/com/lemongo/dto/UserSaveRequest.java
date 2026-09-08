package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSaveRequest(
        @NotBlank(message = "用户名不能为空") String username,
        String password,
        String nickname,
        String email,
        String phone,
        @NotBlank(message = "角色不能为空") String role,
        @NotNull(message = "状态不能为空") Integer status) {
}
