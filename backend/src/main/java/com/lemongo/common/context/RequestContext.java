package com.lemongo.common.context;

import java.time.Instant;

/**
 * Holds request-scoped trace data for the current thread. Values are managed by
 * the trace filter and cleared when the request completes.
 */
public final class RequestContext {

    private static final ThreadLocal<Scope> SCOPES = new ThreadLocal<>();

    private RequestContext() {
    }

    public record Trace(String requestId, String httpMethod, String uri, Instant startTime) {
    }

    private static final class Scope {
        private Trace trace;
        private Long userId;
        private String username;
    }

    public static Trace begin(String requestId, String httpMethod, String uri, Instant startTime) {
        Scope scope = new Scope();
        scope.trace = new Trace(requestId, httpMethod, uri, startTime);
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

    public static void end() {
        SCOPES.remove();
    }
}

