package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.ModuleSaveRequest;
import com.lemongo.entity.SysModule;
import com.lemongo.service.ModuleAdminService;
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
@RequestMapping("/api/admin/modules")
@RequiredArgsConstructor
public class AdminModuleController {

    private final ModuleAdminService moduleAdminService;

    @GetMapping
    public Result<PageResult<SysModule>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(PageResult.of(moduleAdminService.list(page, size, keyword)));
    }

    @PostMapping
    public Result<SysModule> create(@Valid @RequestBody ModuleSaveRequest request) {
        return Result.ok(moduleAdminService.create(request));
    }

    @PutMapping("/{id}")
    public Result<SysModule> update(@PathVariable Long id,
                                    @Valid @RequestBody ModuleSaveRequest request) {
        return Result.ok(moduleAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        moduleAdminService.delete(id);
        return Result.ok();
    }
}
