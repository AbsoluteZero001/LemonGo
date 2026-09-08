package com.lemongo.vo;

public record ApiRegistryVo(
        Long id,
        String apiPath,
        String httpMethod,
        Long moduleId,
        String moduleName,
        String controllerName,
        String controllerMethod,
        String serviceName,
        String mapperName,
        String description,
        Long developerId,
        String developerName,
        Integer status) {
}
