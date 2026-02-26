package com.ferrisys.repository;

import com.ferrisys.common.entity.org.Warehouse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<Warehouse> findByTenantIdAndBranch_IdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, UUID branchId, String search, Pageable pageable);
}
