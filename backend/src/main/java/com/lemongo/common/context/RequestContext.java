package com.lemongo.common.context;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds request-scoped trace data for the current thread. Values are managed by
 * the trace filter and cleared when the request completes.
 */
public final class RequestContext {

    private static final ThreadLocal<Scope> SCOPES = new ThreadLocal<>();

    private RequestContext() {
    }

    public record Trace(
            String requestId,
            String httpMethod,
            String uri,
            Instant startTime,
            String clientIp,
            String queryString) {
    }

    public record LayerSpan(
            String layerType,
            String layerName,
            String layerMethod,
            long durationMs) {
    }

    private static final class Scope {
        private Trace trace;
        private Long userId;
        private String username;
        private final Map<String, LayerSpan> primaryLayers = new LinkedHashMap<>();
        private String errorType;
        private String exceptionClass;
        private String errorMessage;
        private boolean errorLogSaved;
    }

    public static Trace begin(String requestId, String httpMethod, String uri, Instant startTime) {
        return begin(requestId, httpMethod, uri, startTime, null, null);
    }

    public static Trace begin(
            String requestId,
            String httpMethod,
            String uri,
            Instant startTime,
            String clientIp,
            String queryString) {
        Scope scope = new Scope();
        scope.trace = new Trace(requestId, httpMethod, uri, startTime, clientIp, queryString);
        SCOPES.set(scope);
        return scope.trace;
    }

    public static Trace trace() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.trace;
    }

    public static void setUser(Long userId, String username) {
        Scope scope = SCOPES.get();
        if (scope != null) {
            scope.userId = userId;
            scope.username = username;
        }
    }

    public static Long userId() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.userId;
    }

    public static String username() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.username;
    }

    public static String requestIdOrEmpty() {
        Trace trace = trace();
        return trace == null ? "" : trace.requestId();
    }

    public static void recordLayer(LayerSpan span) {
        Scope scope = SCOPES.get();
        if (scope != null && span != null) {
            scope.primaryLayers.putIfAbsent(span.layerType(), span);
        }
    }

    public static List<LayerSpan> layers() {
        Scope scope = SCOPES.get();
        if (scope == null) {
            return List.of();
        }
        List<LayerSpan> spans = new ArrayList<>(scope.primaryLayers.values());
        return Collections.unmodifiableList(spans);
    }

    public static LayerSpan layer(String layerType) {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.primaryLayers.get(layerType);
    }

    public static void recordFailure(String errorType, String exceptionClass, String errorMessage) {
        Scope scope = SCOPES.get();
        if (scope != null) {
            scope.errorType = errorType;
            scope.exceptionClass = exceptionClass;
            scope.errorMessage = errorMessage;
        }
    }

    public static String errorType() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.errorType;
    }

    public static String exceptionClass() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.exceptionClass;
    }

    public static String errorMessage() {
        Scope scope = SCOPES.get();
        return scope == null ? null : scope.errorMessage;
    }

    public static void markErrorLogSaved() {
        Scope scope = SCOPES.get();
        if (scope != null) {
            scope.errorLogSaved = true;
        }
    }

    public static boolean isErrorLogSaved() {
        Scope scope = SCOPES.get();
        return scope != null && scope.errorLogSaved;
    }

    public static void end() {
        SCOPES.remove();
    }
}
