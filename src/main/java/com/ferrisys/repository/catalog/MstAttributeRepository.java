package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstAttribute;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstAttributeRepository extends JpaRepository<MstAttribute, UUID> {
    Page<MstAttribute> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId, String search, Boolean active, Pageable pageable);
    Optional<MstAttribute> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String code);
    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNullAndActiveTrueAndIdNot(UUID tenantId, String code, UUID id);
    List<MstAttribute> findByTenantIdAndDeletedAtIsNullAndRequiredTrueAndActiveTrue(UUID tenantId);
}
