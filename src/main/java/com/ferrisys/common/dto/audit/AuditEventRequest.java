package com.ferrisys.common.dto.audit;

import java.time.LocalDateTime;

public record AuditEventRequest(
        LocalDateTime from,
        LocalDateTime to,
        String action,
        String entityType,
        String entityId,
        int page,
        int size) {
}
