package com.ferrisys.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, String message, Long total, Integer page, Integer size, Integer totalPages) {

    public static <T> ApiResponse<T> single(T data) {
        return new ApiResponse<>(data, "OK", null, null, null, null);
    }

    public static <T> ApiResponse<T> list(T data, long total, int page, int size, int totalPages) {
        return new ApiResponse<>(data, null, total, page, size, totalPages);
    }
}
