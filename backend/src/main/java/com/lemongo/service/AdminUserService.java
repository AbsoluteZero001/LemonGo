package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.common.api.ResultCode;
import com.lemongo.dto.UserSaveRequest;
import com.lemongo.entity.SysUser;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.vo.UserAdminVo;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final SysUserMapper userMapper;

    public Page<UserAdminVo> list(long page, long size, String keyword, String role, Integer status) {
        Page<SysUser> userPage = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>()
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getNickname, keyword))
                        .eq(StringUtils.hasText(role), SysUser::getRole, role)
                        .eq(status != null, SysUser::getStatus, status)
                        .orderByAsc(SysUser::getId));
        Page<UserAdminVo> result = new Page<>(userPage.getCurrent(), userPage.getSize());
        result.setTotal(userPage.getTotal());
        result.setRecords(userPage.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    public UserAdminVo create(UserSaveRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户名已存在");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "初始密码不能为空");
        }
        LocalDateTime now = LocalDateTime.now(ZONE);
        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash("{seed}" + request.password());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus(request.status());
        user.setOnlineStatus(0);
        user.setActivityScore(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return toVo(user);
    }

    public UserAdminVo update(Long id, UserSaveRequest request) {
        SysUser user = require(id);
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus(request.status());
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash("{seed}" + request.password());
        }
        user.setUpdatedAt(LocalDateTime.now(ZONE));
        userMapper.updateById(user);
        return toVo(user);
    }

    private SysUser require(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    private UserAdminVo toVo(SysUser user) {
        return new UserAdminVo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getRole(),
                user.getOnlineStatus(),
                user.getFirstLoginTime(),
                user.getLastLoginTime(),
                user.getLastActiveTime(),
                user.getActivityScore(),
                user.getCreatedAt());
    }
}
