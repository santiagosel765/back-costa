package com.ferrisys.repository;

import com.ferrisys.common.entity.org.UserBranchAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBranchAssignmentRepository extends JpaRepository<UserBranchAssignment, UUID> {
    List<UserBranchAssignment> findByTenantIdAndUserIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID userId);

    Page<UserBranchAssignment> findByTenantIdAndUserIdAndDeletedAtIsNull(UUID tenantId, UUID userId, Pageable pageable);

    Page<UserBranchAssignment> findByTenantIdAndBranch_IdAndDeletedAtIsNull(UUID tenantId, UUID branchId, Pageable pageable);

    Page<UserBranchAssignment> findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(UUID tenantId, UUID userId, UUID branchId, Pageable pageable);

    Page<UserBranchAssignment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<UserBranchAssignment> findByTenantIdAndUserIdAndBranch_Id(UUID tenantId, UUID userId, UUID branchId);

    Optional<UserBranchAssignment> findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(UUID tenantId, UUID userId, UUID branchId);

    Optional<UserBranchAssignment> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
