package com.ferrisys.repository;

import com.ferrisys.common.entity.org.Branch;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Optional<Branch> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<Branch> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, String search, Pageable pageable);
}
