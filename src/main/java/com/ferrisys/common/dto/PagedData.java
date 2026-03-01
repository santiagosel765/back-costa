package com.ferrisys.common.dto;

import java.util.List;

public record PagedData<T>(
        List<T> data,
        long total,
        int page,
        int size,
        int totalPages
) {
    public static <T> PagedData<T> from(PageResponse<T> response) {
        return new PagedData<>(
                response.content(),
                response.totalElements(),
                response.page(),
                response.size(),
                response.totalPages()
        );
    }
}

