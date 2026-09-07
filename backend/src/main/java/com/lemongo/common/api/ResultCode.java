package com.lemongo.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    OK(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    NOT_FOUND(404, "resource not found"),
    INTERNAL_ERROR(500, "internal server error"),
    SERVICE_UNAVAILABLE(503, "service unavailable");

    private final int code;
    private final String defaultMessage;
}

