package com.ferrisys.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        int page,
        int size
) {
    public static <T> PageResponse<T> of(List<T> c, int totalPages, long totalElements, int page, int size) {
        return new PageResponse<>(c, totalPages, totalElements, page, size);
    }

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }
}
