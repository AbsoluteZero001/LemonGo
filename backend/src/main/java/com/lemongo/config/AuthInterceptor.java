package com.lemongo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemongo.common.api.Result;
import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            try {
                Claims claims = tokenService.parseToken(authorization.substring(7));
                RequestContext.setUser(
                        Long.valueOf(claims.getSubject()),
                        claims.get("username", String.class));
                RequestContext.setRole(claims.get("role", String.class));
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Ignoring invalid bearer token");
            }
        }

        String requiredRole = requiredRole(request.getRequestURI());
        if (requiredRole != null && !requiredRole.equals(RequestContext.role())) {
            writeForbidden(response);
            return false;
        }
        return true;
    }

    private String requiredRole(String uri) {
        if (uri.startsWith("/api/admin")) {
            return "ADMIN";
        }
        if (uri.startsWith("/api/monitor")) {
            return "MONITOR";
        }
        return null;
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(ResultCode.FORBIDDEN.getCode());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(ResultCode.FORBIDDEN, "无权限访问")));
    }
}
