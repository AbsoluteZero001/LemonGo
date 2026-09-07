package com.lemongo.aspect;

import com.lemongo.common.context.RequestContext;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Records the first visible controller, service and mapper layer for every request.
 */
@Aspect
@Component
public class ChainTraceAspect {

    private static final String CONTROLLER = "CONTROLLER";
    private static final String SERVICE = "SERVICE";
    private static final String MAPPER = "MAPPER";

    @Around("execution(public * com.lemongo.controller..*(..))")
    public Object traceController(ProceedingJoinPoint joinPoint) throws Throwable {
        return record(joinPoint, CONTROLLER);
    }

    @Around("execution(public * com.lemongo.service..*(..))")
    public Object traceService(ProceedingJoinPoint joinPoint) throws Throwable {
        return record(joinPoint, SERVICE);
    }

    @Around("execution(public * com.lemongo.mapper..*(..))")
    public Object traceMapper(ProceedingJoinPoint joinPoint) throws Throwable {
        return record(joinPoint, MAPPER);
    }

    private Object record(ProceedingJoinPoint joinPoint, String layerType) throws Throwable {
        if (RequestContext.trace() == null) {
            return joinPoint.proceed();
        }
        long startedNanos = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            addSpan(joinPoint, layerType, startedNanos, null);
            return result;
        } catch (Throwable ex) {
            addSpan(joinPoint, layerType, startedNanos, ex);
            RequestContext.recordFailure(
                    "SYSTEM",
                    ex.getClass().getName(),
                    message(ex));
            throw ex;
        }
    }

    private void addSpan(ProceedingJoinPoint joinPoint, String layerType, long startedNanos,
                         Throwable failure) {
        long durationMs = (System.nanoTime() - startedNanos) / 1_000_000;
        String declaringType = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String layerName = layerType.equals(MAPPER)
                ? mapperLayerName(joinPoint)
                : declaringType;
        String simpleName = declaringType.endsWith("Impl")
                ? declaringType.substring(0, declaringType.length() - 4)
                : declaringType;
        if (layerType.equals(MAPPER)) {
            simpleName = layerName == null ? simpleName : layerName;
        }
        String methodName = joinPoint.getSignature().getName();
        RequestContext.recordLayer(new RequestContext.LayerSpan(
                layerType,
                simpleName,
                methodName,
                durationMs));
    }

    private String mapperLayerName(ProceedingJoinPoint joinPoint) {
        String declared = joinPoint.getSignature().getDeclaringType().getSimpleName();
        if (!"BaseMapper".equals(declared)) {
            return declared;
        }
        for (Class<?> type : joinPoint.getTarget().getClass().getInterfaces()) {
            String simpleName = type.getSimpleName();
            if (simpleName.endsWith("Mapper") && !"BaseMapper".equals(simpleName)
                    && type.getName().startsWith("com.lemongo")) {
                return simpleName;
            }
        }
        return null;
    }

    private String message(Throwable failure) {
        if (failure == null || failure.getMessage() == null) {
            return null;
        }
        String[] lines = failure.getMessage().split("\\R", 2);
        return Arrays.stream(lines).findFirst().orElse(null);
    }
}
