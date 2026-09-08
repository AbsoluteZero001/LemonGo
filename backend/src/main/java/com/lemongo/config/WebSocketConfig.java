package com.lemongo.config;

import com.lemongo.observability.MonitorWebSocketHandler;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MonitorWebSocketHandler monitorWebSocketHandler;
    private final JwtTokenService tokenService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(monitorWebSocketHandler, "/ws/monitor")
                .addInterceptors(new MonitorAuthHandshakeInterceptor(tokenService))
                .setAllowedOriginPatterns("*");
    }

    @RequiredArgsConstructor
    private static class MonitorAuthHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtTokenService tokenService;

        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes) {
            String token = token(request);
            if (token == null) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
            try {
                Claims claims = tokenService.parseToken(token);
                if (!"MONITOR".equals(claims.get("role", String.class))) {
                    response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                    return false;
                }
                return true;
            } catch (Exception ex) {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        private String token(ServerHttpRequest request) {
            if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
                HttpServletRequest http = servletRequest.getServletRequest();
                return http.getParameter("token");
            }
            return UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("token");
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception) {
            // nothing to do
        }
    }
}
