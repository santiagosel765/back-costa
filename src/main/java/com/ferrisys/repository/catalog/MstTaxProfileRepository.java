package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstTaxProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstTaxProfileRepository extends JpaRepository<MstTaxProfile, UUID> {
    Page<MstTaxProfile> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId, String search, Boolean active, Pageable pageable);
    Optional<MstTaxProfile> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String name);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrueAndIdNot(UUID tenantId, String name, UUID id);
}
