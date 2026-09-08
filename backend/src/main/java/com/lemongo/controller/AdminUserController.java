package com.lemongo.controller;

import com.lemongo.common.api.PageResult;
import com.lemongo.common.api.Result;
import com.lemongo.dto.UserSaveRequest;
import com.lemongo.service.AdminUserService;
import com.lemongo.vo.UserAdminVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<UserAdminVo>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageResult.of(adminUserService.list(page, size, keyword, role, status)));
    }

    @PostMapping
    public Result<UserAdminVo> create(@Valid @RequestBody UserSaveRequest request) {
        return Result.ok(adminUserService.create(request));
    }

    @PutMapping("/{id}")
    public Result<UserAdminVo> update(@PathVariable Long id,
                                      @Valid @RequestBody UserSaveRequest request) {
        return Result.ok(adminUserService.update(id, request));
    }
}
