package com.ferrisys.repository;

import com.ferrisys.common.entity.org.Warehouse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    @Query("""
            SELECT w FROM Warehouse w
            WHERE w.tenantId = :tenantId
              AND w.deletedAt IS NULL
              AND (:branchId IS NULL OR w.branch.id = :branchId)
              AND (:active IS NULL OR w.active = :active)
              AND (
                    :search = ''
                    OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<Warehouse> search(UUID tenantId, UUID branchId, Boolean active, String search, Pageable pageable);

    boolean existsByTenantIdAndBranch_IdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, UUID branchId, String code);

    boolean existsByTenantIdAndBranch_IdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, UUID branchId, String code, UUID id);
}
