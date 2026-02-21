package com.ferrisys.service.audit;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AuditEventPublishRequest(
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
        Map<String, Object> payload) {
}
