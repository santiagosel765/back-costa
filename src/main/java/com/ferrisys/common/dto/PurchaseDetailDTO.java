package com.ferrisys.common.dto;

import java.math.BigDecimal;

public record PurchaseDetailDTO(String productId, Integer quantity, BigDecimal unitPrice) {
}
