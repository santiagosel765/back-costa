package com.ferrisys.repository;

import com.ferrisys.common.entity.license.ModuleLicense;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleLicenseRepository extends JpaRepository<ModuleLicense, UUID> {

    Optional<ModuleLicense> findByTenantIdAndModule_Id(UUID tenantId, UUID moduleId);
}
