package com.lemongo.observability;

import com.lemongo.common.api.ResultCode;
import com.lemongo.common.context.RequestContext;
import com.lemongo.entity.ErrorLog;
import com.lemongo.entity.RequestLog;
import com.lemongo.observability.mapper.ApiStatisticsMapper;
import com.lemongo.observability.mapper.ErrorLogMapper;
import com.lemongo.observability.mapper.ModuleStatisticsMapper;
import com.lemongo.observability.mapper.RequestLogMapper;
import com.lemongo.observability.mapper.UserActivityMapper;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.responsibility.ApiRegistry;
import com.lemongo.responsibility.ApiRegistry.Responsibility;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraceCompletionService {

    private static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final RequestLogMapper requestLogMapper;
    private final ErrorLogMapper errorLogMapper;
    private final UserActivityMapper userActivityMapper;
    private final ApiStatisticsMapper apiStatisticsMapper;
    private final ModuleStatisticsMapper moduleStatisticsMapper;
    private final SysUserMapper sysUserMapper;
    private final ApiRegistry apiRegistry;
    private final RedisObservationService redisObservationService;

    public void complete(
            String requestId,
            String httpMethod,
            String uri,
            int httpStatus,
            long durationMs) {
        RequestContext.Trace trace = RequestContext.trace();
        if (trace == null) {
            return;
        }
        if ("/api/auth/heartbeat".equals(uri)) {
            return;
        }

        Responsibility responsibility = apiRegistry.resolve(httpMethod, uri);
        boolean success = httpStatus < 400;
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(requestId);
        requestLog.setUserId(RequestContext.userId());
        requestLog.setUsername(RequestContext.username());
        requestLog.setRequestTime(localTime(trace.startTime()));
        requestLog.setClientIp(trace.clientIp());
        requestLog.setHttpMethod(httpMethod);
        requestLog.setUri(uri);
        requestLog.setParamSummary(trace.queryString());
        applyLayers(requestLog, responsibility);
        if (responsibility != null) {
            requestLog.setModuleId(responsibility.module().getId());
            requestLog.setModuleName(responsibility.module().getModuleName());
            requestLog.setDeveloperId(responsibility.developer().getId());
            requestLog.setDeveloperName(responsibility.developer().getName());
        }
        requestLog.setHttpStatus(httpStatus);
        requestLog.setSuccess(success ? 1 : 0);
        requestLog.setErrorType(RequestContext.errorType());
        requestLog.setErrorMessage(RequestContext.errorMessage());
        requestLog.setStartTime(localTime(trace.startTime()));
        requestLog.setEndTime(LocalDateTime.now(ASIA_SHANGHAI));
        requestLog.setDurationMs((int) durationMs);
        requestLog.setCreatedAt(LocalDateTime.now(ASIA_SHANGHAI));
        requestLogMapper.insert(requestLog);

        if (!success && !RequestContext.isErrorLogSaved()) {
            saveFallbackError(responsibility, httpStatus, requestId, trace, durationMs);
        }
        boolean pagePresenceRequest = !"/api/auth/logout".equals(uri);
        if (RequestContext.userId() != null && pagePresenceRequest) {
            userActivityMapper.addActivity(RequestContext.userId(), durationMs);
            sysUserMapper.touchActivity(RequestContext.userId());
            redisObservationService.recordUser(RequestContext.userId());
        }
        int successFlag = success ? 1 : 0;
        int errorFlag = success ? 0 : 1;
        if (responsibility != null) {
            Long apiId = responsibility.api().getId();
            apiStatisticsMapper.addApiStat(
                    apiId,
                    responsibility.api().getApiPath(),
                    httpMethod,
                    successFlag,
                    errorFlag,
                    durationMs);
            moduleStatisticsMapper.addModuleStat(
                    responsibility.module().getId(),
                    responsibility.module().getModuleName(),
                    successFlag,
                    errorFlag,
                    durationMs);
            redisObservationService.recordApi(apiId, successFlag, errorFlag);
            redisObservationService.recordModule(
                    responsibility.module().getId(),
                    successFlag,
                    errorFlag);
        }
        RequestContext.markErrorLogSaved();
    }

    public void saveError(
            Responsibility responsibility,
            int httpStatus,
            Throwable exception) {
        RequestContext.Trace trace = RequestContext.trace();
        if (trace == null) {
            return;
        }
        ErrorLog errorLog = new ErrorLog();
        errorLog.setRequestId(trace.requestId());
        errorLog.setApiId(responsibility == null ? null : responsibility.api().getId());
        errorLog.setModuleId(responsibility == null ? null : responsibility.module().getId());
        errorLog.setDeveloperId(responsibility == null ? null : responsibility.developer().getId());
        errorLog.setErrorCode(httpStatus);
        errorLog.setErrorType(typeFor(httpStatus, exception));
        errorLog.setErrorMessage(shortMessage(exception));
        errorLog.setExceptionClass(exception == null ? null : exception.getClass().getName());
        errorLog.setStackTrace(stackTrace(exception));
        errorLog.setOccurredAt(LocalDateTime.now(ASIA_SHANGHAI));
        errorLog.setCreatedAt(LocalDateTime.now(ASIA_SHANGHAI));
        errorLogMapper.insert(errorLog);
        RequestContext.markErrorLogSaved();
    }

    private void applyLayers(RequestLog requestLog, Responsibility responsibility) {
        List<RequestContext.LayerSpan> layers = RequestContext.layers();
        for (RequestContext.LayerSpan span : layers) {
            String name = span.layerName();
            String method = span.layerMethod();
            switch (span.layerType()) {
                case "CONTROLLER" -> {
                    requestLog.setControllerName(name);
                    requestLog.setControllerMethod(method);
                }
                case "SERVICE" -> requestLog.setServiceName(name + "." + method);
                case "MAPPER" -> requestLog.setMapperName(name);
                default -> {
                    // ignore
                }
            }
        }
        if (responsibility != null
                && responsibility.api().getMapperName() != null
                && ("BaseMapper".equals(requestLog.getMapperName())
                || requestLog.getMapperName() == null)) {
            requestLog.setMapperName(responsibility.api().getMapperName());
        }
    }

    private void saveFallbackError(
            Responsibility responsibility,
            int httpStatus,
            String requestId,
            RequestContext.Trace trace,
            long durationMs) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setRequestId(requestId);
        errorLog.setApiId(responsibility == null ? null : responsibility.api().getId());
        errorLog.setModuleId(responsibility == null ? null : responsibility.module().getId());
        errorLog.setDeveloperId(responsibility == null ? null : responsibility.developer().getId());
        errorLog.setErrorCode(httpStatus);
        errorLog.setErrorType(RequestContext.errorType());
        errorLog.setErrorMessage(RequestContext.errorMessage());
        errorLog.setExceptionClass(RequestContext.exceptionClass());
        errorLog.setOccurredAt(LocalDateTime.now(ASIA_SHANGHAI));
        errorLog.setCreatedAt(LocalDateTime.now(ASIA_SHANGHAI));
        errorLogMapper.insert(errorLog);
    }

    private LocalDateTime localTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ASIA_SHANGHAI);
    }

    private String shortMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage().replaceAll("\\R", " ");
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }

    private String stackTrace(Throwable exception) {
        if (exception == null) {
            return null;
        }
        java.io.StringWriter writer = new java.io.StringWriter();
        exception.printStackTrace(new java.io.PrintWriter(writer));
        String value = writer.toString();
        return value.length() > 100_000 ? value.substring(0, 100_000) : value;
    }

    private String typeFor(int status, Throwable exception) {
        if (exception == null) {
            return "HTTP_" + status;
        }
        if (status == ResultCode.BAD_REQUEST.getCode()) {
            return "PARAMETER_ERROR";
        }
        if (status == ResultCode.UNAUTHORIZED.getCode()) {
            return "AUTH_ERROR";
        }
        if (status == ResultCode.NOT_FOUND.getCode()) {
            return "NOT_FOUND";
        }
        if (exception instanceof org.springframework.dao.DataAccessException) {
            return "DATABASE_ERROR";
        }
        return "SYSTEM_ERROR";
    }
}
