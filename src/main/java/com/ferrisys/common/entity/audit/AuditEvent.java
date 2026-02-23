package com.ferrisys.common.entity.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false)
    private String actor;

    @Column(name = "actor_user_id", columnDefinition = "uuid")
    private UUID actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "payload_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payloadJson; // o JsonNode / Map

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
