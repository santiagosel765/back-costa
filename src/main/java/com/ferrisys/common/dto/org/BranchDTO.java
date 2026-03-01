package com.ferrisys.common.dto.org;

import java.math.BigDecimal;

public record BranchDTO(String id,
                        String code,
                        String name,
                        String description,
                        String address,
                        String addressLine1,
                        String addressLine2,
                        String city,
                        String state,
                        String country,
                        String postalCode,
                        BigDecimal latitude,
                        BigDecimal longitude,
                        String locationNotes,
                        Boolean active,
                        String updatedAt) {
}
