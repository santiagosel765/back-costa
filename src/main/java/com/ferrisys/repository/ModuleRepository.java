package com.ferrisys.repository;

import com.ferrisys.common.entity.user.AuthModule;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<AuthModule, UUID> {

    Optional<AuthModule> findByModuleKeyIgnoreCase(String moduleKey);

    Optional<AuthModule> findByModuleKeyIgnoreCaseAndTenantId(String moduleKey, UUID tenantId);

    Page<AuthModule> findByTenantIdAndStatusOrderByNameAsc(UUID tenantId, Integer status, Pageable pageable);

    Optional<AuthModule> findByIdAndTenantId(UUID id, UUID tenantId);
}
