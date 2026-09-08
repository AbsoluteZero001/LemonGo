package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.DeveloperSaveRequest;
import com.lemongo.entity.SysDeveloper;
import com.lemongo.service.DeveloperAdminService;
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
@RequestMapping("/api/admin/developers")
@RequiredArgsConstructor
public class AdminDeveloperController {

    private final DeveloperAdminService developerAdminService;

    @GetMapping
    public Result<PageResult<SysDeveloper>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(PageResult.of(developerAdminService.list(page, size, keyword)));
    }

    @PostMapping
    public Result<SysDeveloper> create(@Valid @RequestBody DeveloperSaveRequest request) {
        return Result.ok(developerAdminService.create(request));
    }

    @PutMapping("/{id}")
    public Result<SysDeveloper> update(@PathVariable Long id,
                                       @Valid @RequestBody DeveloperSaveRequest request) {
        return Result.ok(developerAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        developerAdminService.delete(id);
        return Result.ok();
    }
}
