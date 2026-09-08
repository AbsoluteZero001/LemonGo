package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.config.JwtTokenService;
import com.lemongo.dto.LoginRequest;
import com.lemongo.dto.RegisterRequest;
import com.lemongo.entity.LoginLog;
import com.lemongo.entity.SysUser;
import com.lemongo.exception.BusinessException;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.observability.RedisObservationService;
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
    private final RedisObservationService redisObservationService;

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

        loginLogMapper.closeStaleSession(user.getId(), now);
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
        loginLog.setLastActiveTime(now);
        loginLog.setActiveSeconds(0);
        loginLog.setSessionStatus(1);
        loginLogMapper.insert(loginLog);
        RequestContext.setUser(user.getId(), user.getUsername());
        return new LoginVo(tokenService.createToken(user), toProfile(user));
    }

    public LoginVo register(RegisterRequest request, HttpServletRequest httpRequest) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.username()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash("{seed}" + request.password());
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? request.username() : request.nickname().trim());
        user.setEmail(blankToNull(request.email()));
        user.setPhone(blankToNull(request.phone()));
        user.setStatus(1);
        user.setRole("USER");
        user.setOnlineStatus(1);
        user.setFirstLoginTime(now);
        user.setLastLoginTime(now);
        user.setLastVisitTime(now);
        user.setLastActiveTime(now);
        user.setActivityScore(2);
        user.setCreatedAt(now);
        userMapper.insert(user);

        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginTime(now);
        loginLog.setLoginIp(clientIp(httpRequest));
        loginLog.setUserAgent(httpRequest.getHeader("User-Agent"));
        loginLog.setLoginStatus(1);
        loginLog.setLastActiveTime(now);
        loginLog.setActiveSeconds(0);
        loginLog.setSessionStatus(1);
        loginLog.setCreatedAt(now);
        loginLogMapper.insert(loginLog);

        RequestContext.setUser(user.getId(), user.getUsername());
        return new LoginVo(tokenService.createToken(user), toProfile(user));
    }

    public void heartbeat() {
        Long userId = RequestContext.userId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录");
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        int updated = loginLogMapper.touchOpenSession(userId, now);
        if (updated == 0) {
            loginLogMapper.openLatestSession(userId, now);
        }
        userMapper.touchPresence(userId);
    }

    public void logout() {
        Long userId = RequestContext.userId();
        if (userId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        loginLogMapper.closeOpenSession(userId, now);
        userMapper.markOffline(userId);
        redisObservationService.removeUser(userId);
    }

    public static ProfileVo toProfile(SysUser user) {
        return new ProfileVo(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl());
    }

    private boolean validPassword(SysUser user, String password) {
        return user.getPasswordHash() != null
                && user.getPasswordHash().equals("{seed}" + password);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
