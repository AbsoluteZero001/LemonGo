package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.ApiSaveRequest;
import com.lemongo.entity.SysApi;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysApiMapper;
import com.lemongo.responsibility.ApiRegistry;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ApiAdminService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final SysApiMapper apiMapper;
    private final ApiRegistry apiRegistry;

    public Page<SysApi> list(long page, long size, String keyword) {
        return apiMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysApi>()
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(SysApi::getApiPath, keyword)
                                .or()
                                .like(SysApi::getControllerName, keyword))
                        .orderByAsc(SysApi::getId));
    }

    public SysApi create(ApiSaveRequest request) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        SysApi api = new SysApi();
        apply(api, request);
        api.setCreatedAt(now);
        api.setUpdatedAt(now);
        apiMapper.insert(api);
        apiRegistry.refresh();
        return api;
    }

    public SysApi update(Long id, ApiSaveRequest request) {
        SysApi api = require(id);
        apply(api, request);
        api.setUpdatedAt(LocalDateTime.now(ZONE));
        apiMapper.updateById(api);
        apiRegistry.refresh();
        return api;
    }

    public void delete(Long id) {
        require(id);
        apiMapper.deleteById(id);
        apiRegistry.refresh();
    }

    public void refresh() {
        apiRegistry.refresh();
    }

    private void apply(SysApi api, ApiSaveRequest request) {
        api.setApiPath(request.apiPath());
        api.setHttpMethod(request.httpMethod());
        api.setModuleId(request.moduleId());
        api.setControllerName(request.controllerName());
        api.setControllerMethod(request.controllerMethod());
        api.setServiceName(request.serviceName());
        api.setMapperName(request.mapperName());
        api.setDescription(request.description());
        api.setDeveloperId(request.developerId());
        api.setStatus(request.status());
    }

    private SysApi require(Long id) {
        SysApi api = apiMapper.selectById(id);
        if (api == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接口不存在");
        }
        return api;
    }
}
