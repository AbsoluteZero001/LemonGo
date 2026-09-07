package com.lemongo.service;

import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
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
        Long userId = RequestContext.userId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return AuthService.toProfile(user);
    }
}
