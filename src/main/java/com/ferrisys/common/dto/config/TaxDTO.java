package com.ferrisys.common.dto.config;

import java.math.BigDecimal;

public record TaxDTO(String id, String code, String name, String description, BigDecimal rate) {
}
