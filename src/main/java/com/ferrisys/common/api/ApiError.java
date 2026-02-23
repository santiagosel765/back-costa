package com.ferrisys.common.api;

public record ApiError(String error, String message, int status) {

    public static ApiError of(ErrorCode code, String message, int status) {
        return new ApiError(code.name(), message, status);
    }
}
