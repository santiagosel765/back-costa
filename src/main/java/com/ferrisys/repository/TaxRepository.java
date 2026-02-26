package com.ferrisys.repository;

import com.ferrisys.common.entity.config.Tax;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, UUID> {
    Optional<Tax> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<Tax> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, String search, Pageable pageable);
}
