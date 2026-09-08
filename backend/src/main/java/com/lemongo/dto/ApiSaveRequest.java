package com.lemongo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApiSaveRequest(
        @NotBlank(message = "接口路径不能为空") String apiPath,
        @NotBlank(message = "请求方法不能为空") String httpMethod,
        @NotNull(message = "模块不能为空") Long moduleId,
        String controllerName,
        String controllerMethod,
        String serviceName,
        String mapperName,
        String description,
        @NotNull(message = "负责人不能为空") Long developerId,
        @NotNull(message = "状态不能为空") Integer status) {
}
