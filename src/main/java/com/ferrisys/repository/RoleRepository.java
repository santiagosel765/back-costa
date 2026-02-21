package com.ferrisys.repository;

import com.ferrisys.common.entity.user.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    Page<Role> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Role> findByIdAndTenantId(UUID id, UUID tenantId);
}
