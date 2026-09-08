package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.DeveloperSaveRequest;
import com.lemongo.entity.SysDeveloper;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysDeveloperMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DeveloperAdminService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final SysDeveloperMapper developerMapper;

    public Page<SysDeveloper> list(long page, long size, String keyword) {
        return developerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysDeveloper>()
                        .like(StringUtils.hasText(keyword), SysDeveloper::getName, keyword)
                        .orderByAsc(SysDeveloper::getId));
    }

    public SysDeveloper create(DeveloperSaveRequest request) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        SysDeveloper developer = new SysDeveloper();
        developer.setName(request.name());
        developer.setEmployeeNo(request.employeeNo());
        developer.setEmail(request.email());
        developer.setDepartment(request.department());
        developer.setStatus(request.status());
        developer.setCreatedAt(now);
        developer.setUpdatedAt(now);
        developerMapper.insert(developer);
        return developer;
    }

    public SysDeveloper update(Long id, DeveloperSaveRequest request) {
        SysDeveloper developer = require(id);
        developer.setName(request.name());
        developer.setEmployeeNo(request.employeeNo());
        developer.setEmail(request.email());
        developer.setDepartment(request.department());
        developer.setStatus(request.status());
        developer.setUpdatedAt(LocalDateTime.now(ZONE));
        developerMapper.updateById(developer);
        return developer;
    }

    public void delete(Long id) {
        require(id);
        developerMapper.deleteById(id);
    }

    private SysDeveloper require(Long id) {
        SysDeveloper developer = developerMapper.selectById(id);
        if (developer == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "开发者不存在");
        }
        return developer;
    }
}
