package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstAttributeOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstAttributeOptionRepository extends JpaRepository<MstAttributeOption, UUID> {
    List<MstAttributeOption> findByTenantIdAndAttributeIdAndDeletedAtIsNullAndActiveTrueOrderBySortOrderAsc(UUID tenantId, UUID attributeId);
    List<MstAttributeOption> findByTenantIdAndAttributeIdAndDeletedAtIsNull(UUID tenantId, UUID attributeId);
    Optional<MstAttributeOption> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
