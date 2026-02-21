package com.ferrisys.common.dto;

import java.math.BigDecimal;

public record QuoteDetailDTO(String productId, Integer quantity, BigDecimal unitPrice) {
}
