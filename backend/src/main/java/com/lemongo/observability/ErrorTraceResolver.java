package com.lemongo.observability;

import com.lemongo.common.context.RequestContext;
import com.lemongo.exception.BusinessException;
import com.lemongo.responsibility.ApiRegistry;
import com.lemongo.responsibility.ApiRegistry.Responsibility;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorTraceResolver {

    private final ApiRegistry apiRegistry;
    private final TraceCompletionService traceCompletionService;

    public ErrorTraceVo resolve(Throwable ex, int httpStatus) {
        RequestContext.Trace trace = RequestContext.trace();
        if (trace == null) {
            return new ErrorTraceVo(
                    httpStatus, "请求处理失败", "SYSTEM_ERROR",
                    "", "", "", null, null, null, null, null, null, null,
                    LocalDateTime.now());
        }

        Responsibility responsibility = apiRegistry.resolve(trace.httpMethod(), trace.uri());
        traceCompletionService.saveError(responsibility, httpStatus, ex);
        RequestContext.recordFailure(
                errorType(httpStatus, ex),
                ex.getClass().getName(),
                ex.getMessage());

        RequestContext.LayerSpan controller = RequestContext.layer("CONTROLLER");
        RequestContext.LayerSpan service = RequestContext.layer("SERVICE");
        RequestContext.LayerSpan mapper = RequestContext.layer("MAPPER");
        ErrorTraceVo.Owner owner = null;
        String module = null;
        String moduleCode = null;
        if (responsibility != null) {
            module = responsibility.module().getModuleName();
            moduleCode = responsibility.module().getModuleCode();
            owner = new ErrorTraceVo.Owner(
                    responsibility.developer().getId(),
                    responsibility.developer().getName(),
                    responsibility.developer().getEmployeeNo(),
                    responsibility.developer().getDepartment());
        }
        return new ErrorTraceVo(
                httpStatus,
                safeMessage(ex, httpStatus),
                errorType(httpStatus, ex),
                trace.requestId(),
                trace.uri(),
                trace.httpMethod(),
                controller == null
                        ? responsibility == null ? null : responsibility.api().getControllerName()
                        : controller.layerName(),
                controller == null
                        ? responsibility == null ? null : responsibility.api().getControllerMethod()
                        : controller.layerMethod(),
                service == null
                        ? responsibility == null ? null : responsibility.api().getServiceName()
                        : service.layerName() + "." + service.layerMethod(),
                mapper == null
                        ? responsibility == null ? null : responsibility.api().getMapperName()
                        : mapper.layerName(),
                module,
                moduleCode,
                owner,
                LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
    }

    private String errorType(int status, Throwable ex) {
        if (status == 400) {
            return "PARAMETER_ERROR";
        }
        if (status == 401) {
            return "AUTH_ERROR";
        }
        if (status == 404) {
            return "NOT_FOUND";
        }
        if (ex instanceof DataAccessException) {
            return "DATABASE_ERROR";
        }
        return "SYSTEM_ERROR";
    }

    private String safeMessage(Throwable ex, int status) {
        if (ex instanceof BusinessException || status == 400 || status == 404) {
            return ex.getMessage() == null ? "请求处理失败" : ex.getMessage();
        }
        return "服务处理失败，请携带 requestId 查询详情";
    }
}
