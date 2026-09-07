package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.entity.RequestLog;
import com.lemongo.service.MonitorService;
import com.lemongo.vo.DashboardVo;
import com.lemongo.vo.DeveloperMonitorVo;
import com.lemongo.vo.ModuleMonitorVo;
import com.lemongo.vo.RequestDetailVo;
import com.lemongo.vo.UserActivityVo;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/dashboard")
    public Result<DashboardVo> dashboard() {
        return Result.ok(monitorService.dashboard());
    }

    @GetMapping("/request-logs")
    public Result<PageResult<RequestLog>> requestLogs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) Integer httpStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String uri) {
        return Result.ok(PageResult.of(
                monitorService.requestLogs(page, size, requestId, httpStatus, userId, uri)));
    }

    @GetMapping("/request-logs/{requestId}")
    public Result<RequestDetailVo> requestDetail(@PathVariable String requestId) {
        RequestDetailVo detail = monitorService.requestDetail(requestId);
        return detail == null
                ? Result.fail(404, "请求日志不存在")
                : Result.ok(detail);
    }

    @GetMapping("/users/activity")
    public Result<List<UserActivityVo>> userActivity(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(monitorService.userActivity(date));
    }

    @GetMapping("/modules")
    public Result<List<ModuleMonitorVo>> modules() {
        return Result.ok(monitorService.modules());
    }

    @GetMapping("/developers")
    public Result<List<DeveloperMonitorVo>> developers() {
        return Result.ok(monitorService.developers());
    }
}
