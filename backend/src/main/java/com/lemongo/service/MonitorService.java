package com.lemongo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemongo.entity.ApiStatistics;
import com.lemongo.entity.ErrorLog;
import com.lemongo.entity.ModuleStatistics;
import com.lemongo.entity.RequestLog;
import com.lemongo.entity.SysApi;
import com.lemongo.entity.SysDeveloper;
import com.lemongo.entity.SysModule;
import com.lemongo.entity.SysUser;
import com.lemongo.mapper.SysApiMapper;
import com.lemongo.mapper.SysDeveloperMapper;
import com.lemongo.mapper.SysModuleMapper;
import com.lemongo.mapper.SysUserMapper;
import com.lemongo.observability.RedisObservationService;
import com.lemongo.observability.mapper.ApiStatisticsMapper;
import com.lemongo.observability.mapper.ErrorLogMapper;
import com.lemongo.observability.mapper.LoginLogMapper;
import com.lemongo.observability.mapper.ModuleStatisticsMapper;
import com.lemongo.observability.mapper.RequestLogMapper;
import com.lemongo.vo.ApiRegistryVo;
import com.lemongo.vo.DashboardVo;
import com.lemongo.vo.DeveloperMonitorVo;
import com.lemongo.vo.ErrorLogVo;
import com.lemongo.vo.ModuleMonitorVo;
import com.lemongo.vo.RequestDetailVo;
import com.lemongo.vo.UserActivityVo;
import com.lemongo.vo.UserRequestUsageVo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private static final ZoneId ASIA_SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final RequestLogMapper requestLogMapper;
    private final ErrorLogMapper errorLogMapper;
    private final ApiStatisticsMapper apiStatisticsMapper;
    private final ModuleStatisticsMapper moduleStatisticsMapper;
    private final LoginLogMapper loginLogMapper;
    private final SysApiMapper apiMapper;
    private final SysDeveloperMapper developerMapper;
    private final SysModuleMapper moduleMapper;
    private final SysUserMapper userMapper;
    private final RedisObservationService redisObservationService;

    public DashboardVo dashboard() {
        LocalDateTime todayStart = LocalDate.now(ASIA_SHANGHAI).atStartOfDay();
        long total = requestLogMapper.selectCount(null);
        long today = requestLogMapper.selectCount(
                new LambdaQueryWrapper<RequestLog>()
                        .ge(RequestLog::getRequestTime, todayStart));
        long errors = requestLogMapper.selectCount(
                new LambdaQueryWrapper<RequestLog>()
                        .ge(RequestLog::getRequestTime, todayStart)
                        .eq(RequestLog::getSuccess, 0));

        List<DashboardVo.ModuleMetric> moduleMetrics = moduleMetrics();
        List<DashboardVo.ApiMetric> apiMetrics = apiMetrics();
        List<DashboardVo.TrendPoint> trend = trend();
        return new DashboardVo(
                total,
                today,
                errors,
                redisObservationService.onlineUserCount(),
                trend,
                moduleMetrics,
                apiMetrics);
    }

    public Page<RequestLog> requestLogs(
            long page,
            long size,
            String requestId,
            Integer httpStatus,
            Long userId,
            String uri) {
        LambdaQueryWrapper<RequestLog> wrapper = new LambdaQueryWrapper<RequestLog>()
                .eq(StringUtils.hasText(requestId), RequestLog::getRequestId, requestId)
                .eq(httpStatus != null && httpStatus >= 400, RequestLog::getHttpStatus, httpStatus)
                .eq(httpStatus != null && httpStatus == 200, RequestLog::getSuccess, 1)
                .eq(userId != null, RequestLog::getUserId, userId)
                .like(StringUtils.hasText(uri), RequestLog::getUri, uri)
                .orderByDesc(RequestLog::getRequestTime);
        return requestLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public RequestDetailVo requestDetail(String requestId) {
        RequestLog log = requestLogMapper.selectById(requestId);
        if (log == null) {
            return null;
        }
        ErrorLog error = errorLogMapper.selectOne(
                new LambdaQueryWrapper<ErrorLog>()
                        .eq(ErrorLog::getRequestId, requestId)
                        .orderByDesc(ErrorLog::getOccurredAt)
                        .last("LIMIT 1"));
        List<RequestDetailVo.TraceLayerVo> layers = new ArrayList<>();
        if (StringUtils.hasText(log.getControllerName())) {
            layers.add(new RequestDetailVo.TraceLayerVo(
                    "CONTROLLER",
                    log.getControllerName(),
                    log.getControllerMethod(),
                    "HTTP 适配层"));
        }
        if (StringUtils.hasText(log.getServiceName())) {
            String[] service = log.getServiceName().split("\\.", 2);
            layers.add(new RequestDetailVo.TraceLayerVo(
                    "SERVICE",
                    service[0],
                    service.length > 1 ? service[1] : "",
                    "业务模块"));
        }
        if (StringUtils.hasText(log.getMapperName())) {
            layers.add(new RequestDetailVo.TraceLayerVo(
                    "MAPPER",
                    log.getMapperName(),
                    "",
                    "数据访问层"));
        }
        return new RequestDetailVo(log, layers, error);
    }

    public List<UserActivityVo> userActivity(LocalDate date) {
        LocalDate statDate = date == null ? LocalDate.now(ASIA_SHANGHAI) : date;
        List<SysUser> users = userMapper.selectList(null);
        Map<Long, Map<String, Object>> dailyVisits = loginLogMapper.selectDailyVisits(
                        statDate.atStartOfDay(),
                        statDate.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("userId")).longValue(),
                        Function.identity(),
                        (a, b) -> a));
        Map<Long, Map<String, Object>> totalVisits = loginLogMapper.selectTotalVisits()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("userId")).longValue(),
                        Function.identity(),
                        (a, b) -> a));
        Map<Long, Map<String, Object>> activeSeconds = loginLogMapper.selectActiveSeconds()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("userId")).longValue(),
                        Function.identity(),
                        (a, b) -> a));
        Map<Long, Map<String, Object>> lastLogout = loginLogMapper.selectLastLogout()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("userId")).longValue(),
                        Function.identity(),
                        (a, b) -> a));
        boolean currentDate = statDate.equals(LocalDate.now(ASIA_SHANGHAI));
        Set<Long> onlineIds = currentDate ? redisObservationService.onlineUserIds() : Set.of();
        return users.stream()
                .map(user -> {
                    Map<String, Object> daily = dailyVisits.get(user.getId());
                    Map<String, Object> total = totalVisits.get(user.getId());
                    Map<String, Object> active = activeSeconds.get(user.getId());
                    Map<String, Object> logout = lastLogout.get(user.getId());
                    return new UserActivityVo(
                            user.getId(),
                            user.getUsername(),
                            user.getNickname(),
                            user.getLastLoginTime(),
                            logout == null ? null : dateTime(logout.get("lastLogoutTime")),
                            user.getLastActiveTime(),
                            daily == null ? 0 : number(daily.get("visitCount")).intValue(),
                            total == null ? 0 : number(total.get("visitCount")).intValue(),
                            active == null ? 0 : number(active.get("activeSeconds")).intValue(),
                            user.getActivityScore() == null ? 0 : user.getActivityScore(),
                            currentDate
                                    ? (onlineIds.contains(user.getId()) ? 1 : 0)
                                    : (user.getOnlineStatus() == null
                                    ? 0 : user.getOnlineStatus()));
                })
                .sorted(Comparator.comparingInt(UserActivityVo::onlineStatus).reversed()
                        .thenComparing(
                                Comparator.comparingInt(UserActivityVo::totalVisits).reversed()))
                .toList();
    }

    public List<UserRequestUsageVo> userUsage(LocalDate date) {
        LocalDate statDate = date == null ? LocalDate.now(ASIA_SHANGHAI) : date;
        return requestLogMapper.selectUserUsage(
                        statDate.atStartOfDay(),
                        statDate.plusDays(1).atStartOfDay())
                .stream()
                .map(row -> new UserRequestUsageVo(
                        number(row.get("userId")).longValue(),
                        text(row.get("username")),
                        text(row.get("httpMethod")),
                        text(row.get("uri")),
                        text(row.get("moduleName")),
                        text(row.get("developerName")),
                        number(row.get("requestCount")).longValue(),
                        number(row.get("errorCount")).longValue(),
                        number(row.get("totalDurationMs")).longValue(),
                        dateTime(row.get("lastRequestTime"))))
                .toList();
    }

    public List<ModuleMonitorVo> modules() {
        List<SysModule> modules = moduleMapper.selectList(
                new LambdaQueryWrapper<SysModule>().eq(SysModule::getStatus, 1));
        Map<Long, SysDeveloper> developers = developerMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDeveloper::getId, Function.identity()));
        Map<Long, ModuleStatistics> statsMap = moduleStatisticsMapper.selectList(
                        new LambdaQueryWrapper<ModuleStatistics>()
                                .eq(ModuleStatistics::getStatDate, LocalDate.now(ASIA_SHANGHAI)))
                .stream()
                .collect(Collectors.toMap(ModuleStatistics::getModuleId, Function.identity()));
        return modules.stream()
                .map(module -> {
                    ModuleStatistics stats = statsMap.get(module.getId());
                    SysDeveloper developer = developers.get(module.getDeveloperId());
                    long count = stats == null ? 0 : stats.getRequestCount();
                    long error = stats == null ? 0 : stats.getErrorCount();
                    return new ModuleMonitorVo(
                            module.getId(),
                            module.getModuleName(),
                            module.getModuleCode(),
                            module.getDescription(),
                            module.getDeveloperId(),
                            developer == null ? null : developer.getName(),
                            developer == null ? null : developer.getEmployeeNo(),
                            count,
                            stats == null ? 0 : stats.getSuccessCount(),
                            error,
                            count == 0 ? 0 :
                                    (stats.getTotalDurationMs() == null ? 0 : stats.getTotalDurationMs())
                                            / count,
                            null);
                })
                .sorted(Comparator.comparingLong(ModuleMonitorVo::requestCount).reversed())
                .toList();
    }

    public List<DeveloperMonitorVo> developers() {
        List<SysDeveloper> developers = developerMapper.selectList(
                new LambdaQueryWrapper<SysDeveloper>().eq(SysDeveloper::getStatus, 1));
        List<Map<String, Object>> stats = requestLogMapper.selectDeveloperStats(
                LocalDate.now(ASIA_SHANGHAI).atStartOfDay());
        Map<Long, Map<String, Object>> statsMap = stats.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("developerId")).longValue(),
                        Function.identity(),
                        (a, b) -> a));
        return developers.stream()
                .map(developer -> {
                    Map<String, Object> row = statsMap.get(developer.getId());
                    long count = row == null ? 0 : ((Number) row.get("requestCount")).longValue();
                    long error = row == null ? 0 : ((Number) row.get("errorCount")).longValue();
                    long duration = row == null ? 0
                            : ((Number) row.get("totalDurationMs")).longValue();
                    return new DeveloperMonitorVo(
                            developer.getId(),
                            developer.getName(),
                            developer.getEmployeeNo(),
                            developer.getDepartment(),
                            count,
                            error,
                            duration,
                            count == 0 ? 0 : error * 100.0 / count);
                })
                .sorted(Comparator.comparingLong(DeveloperMonitorVo::requestCount).reversed())
                .toList();
    }

    public List<ApiRegistryVo> apis() {
        List<SysApi> apis = apiMapper.selectList(
                new LambdaQueryWrapper<SysApi>().orderByAsc(SysApi::getId));
        Map<Long, SysModule> modules = moduleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysModule::getId, Function.identity()));
        Map<Long, SysDeveloper> developers = developerMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDeveloper::getId, Function.identity()));
        return apis.stream()
                .map(api -> {
                    SysModule module = modules.get(api.getModuleId());
                    SysDeveloper developer = developers.get(api.getDeveloperId());
                    return new ApiRegistryVo(
                            api.getId(),
                            api.getApiPath(),
                            api.getHttpMethod(),
                            api.getModuleId(),
                            module == null ? null : module.getModuleName(),
                            api.getControllerName(),
                            api.getControllerMethod(),
                            api.getServiceName(),
                            api.getMapperName(),
                            api.getDescription(),
                            api.getDeveloperId(),
                            developer == null ? null : developer.getName(),
                            api.getStatus());
                })
                .toList();
    }

    public Page<ErrorLogVo> errors(
            long page,
            long size,
            String requestId,
            Integer errorCode,
            Long moduleId) {
        Page<ErrorLog> errorPage = errorLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ErrorLog>()
                        .eq(StringUtils.hasText(requestId), ErrorLog::getRequestId, requestId)
                        .eq(errorCode != null, ErrorLog::getErrorCode, errorCode)
                        .eq(moduleId != null, ErrorLog::getModuleId, moduleId)
                        .orderByDesc(ErrorLog::getOccurredAt));
        Map<Long, SysModule> modules = moduleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysModule::getId, Function.identity()));
        Map<Long, SysDeveloper> developers = developerMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDeveloper::getId, Function.identity()));
        List<String> requestIds = errorPage.getRecords().stream()
                .map(ErrorLog::getRequestId)
                .distinct()
                .toList();
        Map<String, RequestLog> requestLogMap = requestIds.isEmpty()
                ? Map.of()
                : requestLogMapper.selectList(
                                new LambdaQueryWrapper<RequestLog>()
                                        .in(RequestLog::getRequestId, requestIds))
                        .stream()
                        .collect(Collectors.toMap(
                                RequestLog::getRequestId,
                                Function.identity(),
                                (a, b) -> a));
        Page<ErrorLogVo> result = new Page<>(errorPage.getCurrent(), errorPage.getSize());
        result.setTotal(errorPage.getTotal());
        result.setRecords(errorPage.getRecords().stream()
                .map(error -> {
                    SysModule module = modules.get(error.getModuleId());
                    SysDeveloper developer = developers.get(error.getDeveloperId());
                    RequestLog requestLog = requestLogMap.get(error.getRequestId());
                    return new ErrorLogVo(
                            error.getId(),
                            error.getRequestId(),
                            error.getApiId(),
                            error.getModuleId(),
                            module == null ? null : module.getModuleName(),
                            error.getDeveloperId(),
                            developer == null ? null : developer.getName(),
                            requestLog == null ? null : requestLog.getUsername(),
                            error.getErrorCode(),
                            error.getErrorType(),
                            error.getErrorMessage(),
                            error.getExceptionClass(),
                            error.getStackTrace(),
                            error.getOccurredAt());
                })
                .toList());
        return result;
    }

    private List<DashboardVo.TrendPoint> trend() {
        LocalDateTime start = LocalDate.now(ASIA_SHANGHAI).minusDays(6).atStartOfDay();
        Map<String, DashboardVo.TrendPoint> map = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now(ASIA_SHANGHAI).minusDays(i);
            String key = day.toString();
            map.put(key, new DashboardVo.TrendPoint(key, 0, 0));
        }
        for (Map<String, Object> row : requestLogMapper.selectTrend(start)) {
            String key = String.valueOf(row.get("statDate"));
            DashboardVo.TrendPoint old = map.get(key);
            if (old != null) {
                map.put(key, new DashboardVo.TrendPoint(
                        key,
                        ((Number) row.get("requestCount")).longValue(),
                        ((Number) row.get("errorCount")).longValue()));
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<DashboardVo.ModuleMetric> moduleMetrics() {
        List<SysModule> modules = moduleMapper.selectList(
                new LambdaQueryWrapper<SysModule>().eq(SysModule::getStatus, 1));
        Map<Long, SysDeveloper> developers = developerMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDeveloper::getId, Function.identity()));
        Map<Long, ModuleStatistics> statsMap = moduleStatisticsMapper.selectList(
                        new LambdaQueryWrapper<ModuleStatistics>()
                                .eq(ModuleStatistics::getStatDate, LocalDate.now(ASIA_SHANGHAI))
                                .orderByDesc(ModuleStatistics::getRequestCount))
                .stream()
                .collect(Collectors.toMap(ModuleStatistics::getModuleId, Function.identity()));
        return modules.stream()
                .map(module -> {
                    ModuleStatistics stats = statsMap.get(module.getId());
                    SysDeveloper developer = developers.get(module.getDeveloperId());
                    long count = stats == null ? 0 : stats.getRequestCount();
                    long duration = stats == null || stats.getTotalDurationMs() == null
                            ? 0 : stats.getTotalDurationMs();
                    return new DashboardVo.ModuleMetric(
                            module.getId(),
                            module.getModuleName(),
                            module.getDeveloperId(),
                            developer == null ? null : developer.getName(),
                            count,
                            stats == null ? 0 : stats.getErrorCount(),
                            count == 0 ? 0 : duration / count);
                })
                .sorted(Comparator.comparingLong(DashboardVo.ModuleMetric::requestCount).reversed())
                .toList();
    }

    private List<DashboardVo.ApiMetric> apiMetrics() {
        return apiStatisticsMapper.selectList(
                        new LambdaQueryWrapper<ApiStatistics>()
                                .eq(ApiStatistics::getStatDate, LocalDate.now(ASIA_SHANGHAI))
                                .orderByDesc(ApiStatistics::getRequestCount)
                                .last("LIMIT 8"))
                .stream()
                .map(stat -> new DashboardVo.ApiMetric(
                        stat.getApiId(),
                        stat.getApiPath(),
                        stat.getHttpMethod(),
                        stat.getRequestCount() == null ? 0 : stat.getRequestCount(),
                        stat.getErrorCount() == null ? 0 : stat.getErrorCount(),
                        stat.getRequestCount() == null || stat.getRequestCount() == 0
                                ? 0 : stat.getTotalDurationMs() / stat.getRequestCount()))
                .toList();
    }

    private Number number(Object value) {
        return value == null ? 0 : (Number) value;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime dateTime(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}
