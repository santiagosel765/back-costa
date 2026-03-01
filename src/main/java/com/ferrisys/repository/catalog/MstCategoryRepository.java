package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstCategoryRepository extends JpaRepository<MstCategory, UUID> {
    Page<MstCategory> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId, String search, Boolean active, Pageable pageable);
    Optional<MstCategory> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrueAndIdNot(UUID tenantId, String name, UUID id);
    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNullAndActiveTrue(UUID tenantId, String name);
    List<MstCategory> findByTenantIdAndDeletedAtIsNullAndActiveTrueOrderBySortOrderAscNameAsc(UUID tenantId);
}
