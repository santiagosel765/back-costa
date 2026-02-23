package com.ferrisys.repository;

import com.ferrisys.common.entity.business.Provider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, UUID> {

    Optional<Provider> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);

    Page<Provider> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
