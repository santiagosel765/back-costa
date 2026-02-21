package com.ferrisys.repository;

import com.ferrisys.common.entity.license.ModuleLicense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleLicenseRepository extends JpaRepository<ModuleLicense, UUID> {

    Optional<ModuleLicense> findByTenantIdAndModule_Id(UUID tenantId, UUID moduleId);

    @Query("SELECT ml FROM ModuleLicense ml JOIN FETCH ml.module WHERE ml.tenantId = :tenantId")
    List<ModuleLicense> findAllByTenantIdWithModule(@Param("tenantId") UUID tenantId);
}
