package com.ferrisys.repository;

import com.ferrisys.common.entity.org.Branch;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Optional<Branch> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<Branch> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, String search, Pageable pageable);
    List<Branch> findByTenantIdAndIdInAndActiveTrueAndDeletedAtIsNull(UUID tenantId, Collection<UUID> ids);
    List<Branch> findByTenantIdAndIdInAndDeletedAtIsNull(UUID tenantId, Collection<UUID> ids);
    boolean existsByTenantIdAndCodeIgnoreCaseAndActiveTrueAndDeletedAtIsNull(UUID tenantId, String code);
    boolean existsByTenantIdAndCodeIgnoreCaseAndIdNotAndActiveTrueAndDeletedAtIsNull(UUID tenantId, String code, UUID id);
}
