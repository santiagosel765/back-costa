package com.ferrisys.common.dto.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID tenantId,
        String actor,
        UUID actorUserId,
        String action,
        String entityType,
        String entityId,
        String requestId,
        String traceId,
        String ipAddress,
        String userAgent,
        String payloadJson,
        LocalDateTime createdAt) {
}
