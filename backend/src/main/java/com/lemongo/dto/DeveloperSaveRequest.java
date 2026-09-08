package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeveloperSaveRequest(
        @NotBlank(message = "姓名不能为空") String name,
        String employeeNo,
        String email,
        String department,
        @NotNull(message = "状态不能为空") Integer status) {
}
