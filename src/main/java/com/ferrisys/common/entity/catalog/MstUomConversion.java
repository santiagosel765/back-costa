package com.ferrisys.common.entity.catalog;

import com.ferrisys.common.audit.Auditable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter @Setter @Entity @Table(name = "mst_uom_conversion")
public class MstUomConversion extends Auditable {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false) private UUID id;
    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid") private UUID tenantId;
    @Column(name = "group_id", nullable = false, columnDefinition = "uuid") private UUID groupId;
    @Column(name = "from_uom_id", nullable = false, columnDefinition = "uuid") private UUID fromUomId;
    @Column(name = "to_uom_id", nullable = false, columnDefinition = "uuid") private UUID toUomId;
    @Column(nullable = false) private BigDecimal factor;
    @Column(nullable = false) private Boolean active = Boolean.TRUE;
    @Column(name = "deleted_at") private OffsetDateTime deletedAt;
    @Column(name = "deleted_by") private String deletedBy;
}
