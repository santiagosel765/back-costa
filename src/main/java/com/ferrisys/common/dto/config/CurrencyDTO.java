package com.ferrisys.common.dto.config;

public record CurrencyDTO(
        String id,
        String code,
        String name,
        String description,
        String symbol,
        Integer decimals,
        Boolean isFunctional,
        Double exchangeRateRef,
        Boolean active,
        String updatedAt
) {
}
