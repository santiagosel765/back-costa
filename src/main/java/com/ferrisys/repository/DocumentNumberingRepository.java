package com.ferrisys.repository;

import com.ferrisys.common.entity.org.DocumentNumbering;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentNumberingRepository extends JpaRepository<DocumentNumbering, UUID> {

    @Query("""
            SELECT dn FROM DocumentNumbering dn
            WHERE dn.tenantId = :tenantId
              AND dn.deletedAt IS NULL
              AND (:branchId IS NULL OR dn.branch.id = :branchId)
              AND (:documentTypeId IS NULL OR dn.documentType.id = :documentTypeId)
              AND (:active IS NULL OR dn.active = :active)
              AND (
                    :search = ''
                    OR LOWER(dn.series) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<DocumentNumbering> search(UUID tenantId, UUID branchId, UUID documentTypeId, Boolean active, String search, Pageable pageable);

    Optional<DocumentNumbering> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    boolean existsByTenantIdAndBranch_IdAndDocumentType_IdAndSeriesIgnoreCaseAndDeletedAtIsNull(
            UUID tenantId, UUID branchId, UUID documentTypeId, String series);

    boolean existsByTenantIdAndBranch_IdAndDocumentType_IdAndSeriesIgnoreCaseAndIdNotAndDeletedAtIsNull(
            UUID tenantId, UUID branchId, UUID documentTypeId, String series, UUID id);
}
