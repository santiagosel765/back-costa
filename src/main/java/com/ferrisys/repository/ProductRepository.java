package com.ferrisys.repository;

import com.ferrisys.common.entity.inventory.Product;
import com.ferrisys.common.enums.catalog.ProductStatus;
import com.ferrisys.common.enums.catalog.ProductType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Product> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    boolean existsByTenantIdAndSkuIgnoreCaseAndIdNotAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String sku, UUID id);

    boolean existsByTenantIdAndSkuIgnoreCaseAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String sku);

    Page<Product> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId, String search, Boolean active, Pageable pageable);

    Page<Product> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActiveAndTypeAndStatus(UUID tenantId, String search, Boolean active, ProductType type, ProductStatus status, Pageable pageable);
}
