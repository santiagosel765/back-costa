package com.ferrisys.common.dto.config;

public record CurrencyDTO(String id, String code, String name, String description, Boolean active, String updatedAt) {
}
