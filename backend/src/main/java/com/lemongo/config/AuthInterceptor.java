package com.lemongo.config;

import com.lemongo.common.context.RequestContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService tokenService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return true;
        }
        try {
            Claims claims = tokenService.parseToken(authorization.substring(7));
            RequestContext.setUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Ignoring invalid bearer token");
        }
        return true;
    }
}
