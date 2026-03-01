package com.ferrisys.common.dto.org;

import java.util.UUID;

public record UpdateDocumentNumberingRequest(
        UUID branchId,
        UUID documentTypeId,
        String series,
        Integer nextNumber,
        Integer padding,
        Boolean active
) {
}
