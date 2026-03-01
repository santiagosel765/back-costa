package com.ferrisys.common.dto.org;

import java.math.BigDecimal;

public record WarehouseDTO(String id,
                           String branchId,
                           String code,
                           String name,
                           String description,
                           String addressLine1,
                           String addressLine2,
                           String city,
                           String state,
                           String country,
                           String postalCode,
                           BigDecimal latitude,
                           BigDecimal longitude,
                           String locationNotes,
                           String warehouseType,
                           Boolean active,
                           String updatedAt) {
}
