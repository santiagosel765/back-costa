package com.ferrisys.repository;

import com.ferrisys.common.entity.inventory.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Page<Category> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);
}
