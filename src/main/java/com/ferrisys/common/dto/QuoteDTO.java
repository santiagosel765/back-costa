package com.ferrisys.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QuoteDTO(
        String id,
        String clientId,
        String description,
        LocalDate date,
        BigDecimal total,
        Integer status,
        List<QuoteDetailDTO> details
) {
}
