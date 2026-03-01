package com.ferrisys.common.dto.org;

public record DocumentNumberingDTO(
        String id,
        String branchId,
        String documentTypeId,
        String series,
        Integer nextNumber,
        Integer padding,
        Boolean active,
        String updatedAt
) {
}
