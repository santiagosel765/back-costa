package com.ferrisys.common.entity.catalog;

import com.ferrisys.common.audit.Auditable;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter @Setter @Entity @Table(name = "mst_uom_group")
public class MstUomGroup extends Auditable {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false) private UUID id;
    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid") private UUID tenantId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private Boolean active = Boolean.TRUE;
    @Column(name = "deleted_at") private OffsetDateTime deletedAt;
    @Column(name = "deleted_by") private String deletedBy;
}
