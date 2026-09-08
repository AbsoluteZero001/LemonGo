package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.entity.SysUser;
import com.lemongo.entity.UserActivity;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.observability.mapper.UserActivityMapper;
import com.lemongo.vo.ProfileVo;
import com.lemongo.vo.UserActivityVo;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final UserActivityMapper userActivityMapper;

    public ProfileVo me() {
        return AuthService.toProfile(requireUser());
    }

    public UserActivityVo meActivity() {
        SysUser user = requireUser();
        UserActivity activity = userActivityMapper.selectOne(
                new LambdaQueryWrapper<UserActivity>()
                        .eq(UserActivity::getUserId, user.getId())
                        .eq(UserActivity::getStatDate, LocalDate.now(ZoneId.of("Asia/Shanghai"))));
        return new UserActivityVo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getLastLoginTime(),
                user.getLastActiveTime(),
                user.getLastVisitTime(),
                activity == null ? 0 : activity.getRequestCountToday(),
                activity == null ? 0 : activity.getRequestCountTotal(),
                activity == null ? 0 : activity.getActiveSecondsToday(),
                activity == null ? 0 : activity.getActiveSecondsTotal(),
                user.getActivityScore() == null ? 0 : user.getActivityScore(),
                user.getOnlineStatus() == null ? 0 : user.getOnlineStatus());
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
}
