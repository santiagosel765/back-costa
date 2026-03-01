package com.ferrisys.common.dto.org;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateWarehouseRequest(
        UUID branchId,
        String code,
        String name,
        String description,
        Boolean active,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        BigDecimal latitude,
        BigDecimal longitude,
        String locationNotes,
        String warehouseType
) {
}
