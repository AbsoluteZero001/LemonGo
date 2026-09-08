package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.ModuleSaveRequest;
import com.lemongo.entity.SysModule;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysModuleMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ModuleAdminService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final SysModuleMapper moduleMapper;

    public Page<SysModule> list(long page, long size, String keyword) {
        return moduleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysModule>()
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(SysModule::getModuleName, keyword)
                                .or()
                                .like(SysModule::getModuleCode, keyword))
                        .orderByAsc(SysModule::getId));
    }

    public SysModule create(ModuleSaveRequest request) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        SysModule module = new SysModule();
        apply(module, request);
        module.setCreatedAt(now);
        module.setUpdatedAt(now);
        moduleMapper.insert(module);
        return module;
    }

    public SysModule update(Long id, ModuleSaveRequest request) {
        SysModule module = require(id);
        apply(module, request);
        module.setUpdatedAt(LocalDateTime.now(ZONE));
        moduleMapper.updateById(module);
        return module;
    }

    public void delete(Long id) {
        require(id);
        moduleMapper.deleteById(id);
    }

    private void apply(SysModule module, ModuleSaveRequest request) {
        module.setModuleName(request.moduleName());
        module.setModuleCode(request.moduleCode());
        module.setDescription(request.description());
        module.setDeveloperId(request.developerId());
        module.setStatus(request.status());
    }

    private SysModule require(Long id) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "模块不存在");
        }
        return module;
    }
}
