package com.ferrisys.repository.catalog;

import com.ferrisys.common.entity.catalog.MstTaxProfileTax;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstTaxProfileTaxRepository extends JpaRepository<MstTaxProfileTax, UUID> {
    List<MstTaxProfileTax> findByTenantIdAndTaxProfileIdAndDeletedAtIsNullOrderBySortOrderAsc(UUID tenantId, UUID taxProfileId);
}
