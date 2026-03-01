package com.ferrisys.common.entity.catalog;

import com.ferrisys.common.audit.Auditable;
import com.ferrisys.common.enums.catalog.ApplyToType;
import com.ferrisys.common.enums.catalog.AttributeType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter @Setter @Entity @Table(name = "mst_attribute")
public class MstAttribute extends Auditable {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false) private UUID id;
    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid") private UUID tenantId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AttributeType type;
    @Column(nullable = false) private Boolean required = Boolean.FALSE;
    @Column(nullable = false) private Boolean searchable = Boolean.FALSE;
    @Column(nullable = false) private Boolean visible = Boolean.TRUE;
    @Enumerated(EnumType.STRING) @Column(name = "apply_to", nullable = false) private ApplyToType applyTo = ApplyToType.PRODUCT;
    @Column(nullable = false) private Boolean active = Boolean.TRUE;
    @Column(name = "deleted_at") private OffsetDateTime deletedAt;
    @Column(name = "deleted_by") private String deletedBy;
}
