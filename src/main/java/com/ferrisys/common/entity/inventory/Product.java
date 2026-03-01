package com.ferrisys.common.entity.inventory;

import com.ferrisys.common.audit.Auditable;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
@Table(name = "inv_product")
public class Product extends Auditable implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type = ProductType.PRODUCT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "brand_id", columnDefinition = "uuid")
    private UUID brandId;

    @Column(name = "uom_id", columnDefinition = "uuid")
    private UUID uomId;

    @Column(name = "tax_profile_id", columnDefinition = "uuid")
    private UUID taxProfileId;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "track_stock")
    private Boolean trackStock = Boolean.FALSE;

    @Column(name = "track_lot")
    private Boolean trackLot = Boolean.FALSE;

    @Column(name = "track_serial")
    private Boolean trackSerial = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;
}
