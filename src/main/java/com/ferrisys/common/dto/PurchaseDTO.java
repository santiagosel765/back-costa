package com.ferrisys.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseDTO(
        String id,
        String providerId,
        String description,
        LocalDate date,
        BigDecimal total,
        Integer status,
        List<PurchaseDetailDTO> details
) {
}
