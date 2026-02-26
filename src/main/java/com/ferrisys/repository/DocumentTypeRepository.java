package com.ferrisys.repository;

import com.ferrisys.common.entity.config.DocumentType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {
    Optional<DocumentType> findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<DocumentType> findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID tenantId, String search, Pageable pageable);
}
