package com.lemongo.service;

import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.dto.ProfileUpdateRequest;
import com.lemongo.entity.SysUser;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.vo.ProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;

    public ProfileVo me() {
        return AuthService.toProfile(requireUser());
    }

    public ProfileVo updateMe(ProfileUpdateRequest request) {
        SysUser user = requireUser();
        userMapper.updateProfile(
                user.getId(),
                request.nickname().trim(),
                blankToNull(request.email()),
                blankToNull(request.phone()));
        user.setNickname(request.nickname().trim());
        user.setEmail(blankToNull(request.email()));
        user.setPhone(blankToNull(request.phone()));
        return AuthService.toProfile(user);
    }

    private SysUser requireUser() {
        Long userId = RequestContext.userId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
