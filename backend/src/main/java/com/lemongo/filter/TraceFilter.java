package com.lemongo.filter;

import com.lemongo.common.context.RequestContext;
import com.lemongo.common.util.RequestIdGenerator;
import com.lemongo.config.LemonGoProperties;
import com.lemongo.observability.TraceCompletionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TraceFilter extends OncePerRequestFilter {

    private final LemonGoProperties properties;
    private final TraceCompletionService completionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader(properties.getRequestIdHeader());
        if (!StringUtils.hasText(requestId)) {
            requestId = RequestIdGenerator.next();
        }

        String uri = request.getRequestURI();
        long startedNanos = System.nanoTime();
        RequestContext.begin(
                requestId,
                request.getMethod(),
                uri,
                Instant.now(),
                clientIp(request),
                request.getQueryString());
        MDC.put("requestId", requestId);
        MDC.put("traceUri", uri);
        response.setHeader(properties.getRequestIdHeader(), requestId);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            RequestContext.recordFailure(
                    "SYSTEM",
                    ex.getClass().getName(),
                    trimMessage(ex.getMessage()));
            throw ex;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
            try {
                completionService.complete(
                        requestId,
                        request.getMethod(),
                        uri,
                        response.getStatus(),
                        durationMs);
            } catch (Exception completionError) {
                log.warn("Failed to persist trace for requestId={}",
                        requestId, completionError);
            } finally {
                log.info("http-request method={} uri={} status={} duration={}ms requestId={}",
                        request.getMethod(), uri, response.getStatus(), durationMs, requestId);
                RequestContext.end();
                MDC.clear();
            }
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }

    private String trimMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
