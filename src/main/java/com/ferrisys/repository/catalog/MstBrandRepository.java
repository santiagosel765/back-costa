package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstBrand;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstBrandRepository extends JpaRepository<MstBrand, UUID> {
    Page<MstBrand> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId, String search, Boolean active, Pageable pageable);
    Optional<MstBrand> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String name);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrueAndIdNot(UUID tenantId, String name, UUID id);
}
