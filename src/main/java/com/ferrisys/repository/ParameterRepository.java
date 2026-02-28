package com.ferrisys.repository;

import com.ferrisys.common.entity.config.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterRepository extends JpaRepository<Parameter, UUID> {
    Optional<Parameter> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Optional<Parameter> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
    Page<Parameter> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, String name, Pageable pageable);
    Page<Parameter> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndCodeContainingIgnoreCase(UUID tenantId, String search, Pageable pageable);
    List<Parameter> findByTenantIdAndCodeInAndDeletedAtIsNull(UUID tenantId, List<String> codes);
}
