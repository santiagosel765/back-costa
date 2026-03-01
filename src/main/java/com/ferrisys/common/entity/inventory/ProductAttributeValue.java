package com.ferrisys.common.entity.inventory;

import com.ferrisys.common.audit.Auditable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter @Setter @Entity @Table(name = "inv_product_attribute_value")
public class ProductAttributeValue extends Auditable implements Serializable {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false) private UUID id;
    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid") private UUID tenantId;
    @Column(name = "product_id", nullable = false, columnDefinition = "uuid") private UUID productId;
    @Column(name = "attribute_id", nullable = false, columnDefinition = "uuid") private UUID attributeId;
    @Column(name = "value_text") private String valueText;
    @Column(name = "value_number") private BigDecimal valueNumber;
    @Column(name = "value_bool") private Boolean valueBool;
    @Column(name = "value_date") private LocalDate valueDate;
    @Column(name = "option_id", columnDefinition = "uuid") private UUID optionId;
    @Column(name = "value_json", columnDefinition = "TEXT") private String valueJson;
    @Column(nullable = false) private Boolean active = Boolean.TRUE;
    @Column(name = "deleted_at") private OffsetDateTime deletedAt;
    @Column(name = "deleted_by") private String deletedBy;
}
