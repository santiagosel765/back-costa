package com.ferrisys.common.dto;

public record ProviderDTO(
        String id,
        String name,
        String contact,
        String phone,
        String address,
        String ruc,
        Integer status
) {
}
