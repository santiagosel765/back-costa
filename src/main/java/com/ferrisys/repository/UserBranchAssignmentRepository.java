package com.ferrisys.repository;

import com.ferrisys.common.entity.org.UserBranchAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBranchAssignmentRepository extends JpaRepository<UserBranchAssignment, UUID> {
    List<UserBranchAssignment> findByTenantIdAndUserIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID userId);
}
