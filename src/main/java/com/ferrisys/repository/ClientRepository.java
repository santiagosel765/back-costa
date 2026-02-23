package com.ferrisys.repository;

import com.ferrisys.common.entity.business.Client;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);

    Page<Client> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
