package com.lemongo.controller;

import com.lemongo.common.api.Result;
import com.lemongo.service.TestErrorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/error")
@RequiredArgsConstructor
public class TestErrorController {

    private final TestErrorService testErrorService;

    @GetMapping("/400")
    public Result<Void> badRequest() {
        testErrorService.badRequest();
        return Result.ok();
    }

    @GetMapping("/404")
    public Result<Void> notFound() {
        testErrorService.notFound();
        return Result.ok();
    }

    @GetMapping("/500")
    public Result<Void> serverError() {
        testErrorService.serverError();
        return Result.ok();
    }

    @GetMapping("/database")
    public Result<Void> databaseError() {
        testErrorService.databaseError();
        return Result.ok();
    }

    @GetMapping("/service")
    public Result<Void> serviceError() {
        testErrorService.serviceError();
        return Result.ok();
    }
}
