package com.ferrisys.repository;

import com.ferrisys.common.entity.tenant.Tenant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
