package com.lemongo.filter;

import com.lemongo.common.context.RequestContext;
import com.lemongo.common.util.RequestIdGenerator;
import com.lemongo.config.LemonGoProperties;
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
        RequestContext.begin(requestId, request.getMethod(), uri, Instant.now());
        MDC.put("requestId", requestId);
        MDC.put("traceUri", uri);
        response.setHeader(properties.getRequestIdHeader(), requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
            log.info("http-request method={} uri={} status={} duration={}ms requestId={}",
                    request.getMethod(), uri, response.getStatus(), durationMs, requestId);
            RequestContext.end();
            MDC.clear();
        }
    }
}

