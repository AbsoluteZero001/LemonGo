package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.ApiSaveRequest;
import com.lemongo.entity.SysApi;
import com.lemongo.service.ApiAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/apis")
@RequiredArgsConstructor
public class AdminApiController {

    private final ApiAdminService apiAdminService;

    @GetMapping
    public Result<PageResult<SysApi>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(PageResult.of(apiAdminService.list(page, size, keyword)));
    }

    @PostMapping
    public Result<SysApi> create(@Valid @RequestBody ApiSaveRequest request) {
        return Result.ok(apiAdminService.create(request));
    }

    @PutMapping("/{id}")
    public Result<SysApi> update(@PathVariable Long id,
                                 @Valid @RequestBody ApiSaveRequest request) {
        return Result.ok(apiAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        apiAdminService.delete(id);
        return Result.ok();
    }

    @PostMapping("/refresh")
    public Result<String> refresh() {
        apiAdminService.refresh();
        return Result.ok("注册表已刷新");
    }
}
