package com.ferrisys.repository;

import com.ferrisys.common.entity.inventory.ProductAttributeValue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, UUID> {
    List<ProductAttributeValue> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId);
}
