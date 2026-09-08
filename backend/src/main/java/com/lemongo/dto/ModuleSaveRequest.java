package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuleSaveRequest(
        @NotBlank(message = "模块名称不能为空") String moduleName,
        @NotBlank(message = "模块编码不能为空") String moduleCode,
        String description,
        @NotNull(message = "负责人不能为空") Long developerId,
        @NotNull(message = "状态不能为空") Integer status) {
}
