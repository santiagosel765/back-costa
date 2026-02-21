package com.ferrisys.service.impl.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.audit.AuditEventRequest;
import com.ferrisys.common.dto.audit.AuditEventResponse;
import com.ferrisys.common.entity.audit.AuditEvent;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.repository.AuditEventRepository;
import com.ferrisys.service.audit.AuditEventPublishRequest;
import com.ferrisys.service.audit.AuditEventService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {

    private static final String REDACTED = "***REDACTED***";

    private final AuditEventRepository auditEventRepository;
    private final TenantContextHolder tenantContextHolder;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(AuditEventPublishRequest event) {
        try {
            HttpServletRequest request = currentRequest();
            AuditEvent auditEvent = AuditEvent.builder()
                    .tenantId(resolveTenantId(event.tenantId()))
                    .actor(resolveActor(event.actor()))
                    .actorUserId(event.actorUserId())
                    .action(event.action())
                    .entityType(event.entityType())
                    .entityId(event.entityId())
                    .requestId(firstNonBlank(event.requestId(), header(request, "X-Request-Id")))
                    .traceId(firstNonBlank(event.traceId(), header(request, "X-B3-TraceId"), header(request, "traceparent")))
                    .ipAddress(firstNonBlank(event.ipAddress(), ipAddress(request)))
                    .userAgent(firstNonBlank(event.userAgent(), header(request, "User-Agent")))
                    .payloadJson(toJson(sanitizePayload(event.payload())))
                    .build();
            auditEventRepository.save(auditEvent);
        } catch (Exception ex) {
            log.warn("Failed to persist audit event for action {}", event.action(), ex);
        }
    }

    @Override
    public PageResponse<AuditEventResponse> findEvents(AuditEventRequest request) {
        Specification<AuditEvent> spec = Specification.where(null);
        if (request.from() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), request.from()));
        }
        if (request.to() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), request.to()));
        }
        if (request.action() != null && !request.action().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), request.action()));
        }
        if (request.entityType() != null && !request.entityType().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), request.entityType()));
        }
        if (request.entityId() != null && !request.entityId().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), request.entityId()));
        }

        if (!isSuperAdmin()) {
            UUID tenantId = tenantContextHolder.requireTenantId();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }

        Page<AuditEventResponse> page = auditEventRepository
                .findAll(spec, PageRequest.of(request.page(), request.size()))
                .map(this::toResponse);
        return PageResponse.from(page);
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getTenantId(),
                event.getActor(),
                event.getActorUserId(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getRequestId(),
                event.getTraceId(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getPayloadJson(),
                event.getCreatedAt());
    }

    private Map<String, Object> sanitizePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (isSensitiveKey(key)) {
                sanitized.put(key, REDACTED);
            } else if (value instanceof Map<?, ?> nested) {
                sanitized.put(key, sanitizePayload((Map<String, Object>) nested));
            } else {
                sanitized.put(key, value);
            }
        });
        return sanitized;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String value = key.toLowerCase(Locale.ROOT);
        return value.contains("password") || value.contains("token") || value.contains("secret") || value.contains("credential");
    }

    private UUID resolveTenantId(UUID preferred) {
        if (preferred != null) {
            return preferred;
        }
        try {
            return tenantContextHolder.requireTenantId();
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveActor(String preferred) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String header(HttpServletRequest request, String key) {
        return request != null ? request.getHeader(key) : null;
    }

    private String ipAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(authority ->
                "ROLE_SUPER_ADMIN".equals(authority) || "SUPER_ADMIN".equals(authority));
    }
}
