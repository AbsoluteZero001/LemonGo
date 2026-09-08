package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称长度不能超过 50 位")
        String nickname,
        @Pattern(
                regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过 100 位")
        String email,
        @Size(max = 30, message = "手机号长度不能超过 30 位")
        String phone) {
}
