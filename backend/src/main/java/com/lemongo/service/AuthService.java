package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.config.JwtTokenService;
import com.lemongo.dto.LoginRequest;
import com.lemongo.entity.LoginLog;
import com.lemongo.entity.SysUser;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.observability.mapper.LoginLogMapper;
import com.lemongo.vo.LoginVo;
import com.lemongo.vo.ProfileVo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenService tokenService;

    public LoginVo login(LoginRequest request, HttpServletRequest httpRequest) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginTime(now);
        loginLog.setLoginIp(clientIp(httpRequest));
        loginLog.setUserAgent(httpRequest.getHeader("User-Agent"));
        loginLog.setCreatedAt(now);

        if (user == null || !validPassword(user, request.password())) {
            loginLog.setUsername(request.username());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("用户名或密码错误");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            loginLog.setUserId(user.getId());
            loginLog.setUsername(user.getUsername());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已停用");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "账号已停用");
        }

        user.setOnlineStatus(1);
        if (user.getFirstLoginTime() == null) {
            user.setFirstLoginTime(now);
        }
        user.setLastLoginTime(now);
        user.setLastVisitTime(now);
        user.setLastActiveTime(now);
        user.setActivityScore(Math.min(100, user.getActivityScore() == null
                ? 2 : user.getActivityScore() + 2));
        userMapper.updateById(user);

        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginStatus(1);
        loginLogMapper.insert(loginLog);
        RequestContext.setUser(user.getId(), user.getUsername());
        return new LoginVo(tokenService.createToken(user), toProfile(user));
    }

    public static ProfileVo toProfile(SysUser user) {
        return new ProfileVo(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getLastLoginTime(),
                user.getLastActiveTime(),
                user.getActivityScore());
    }

    private boolean validPassword(SysUser user, String password) {
        return user.getPasswordHash() != null
                && user.getPasswordHash().equals("{seed}" + password);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
