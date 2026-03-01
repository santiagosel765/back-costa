package com.ferrisys.repository.catalog;
import com.ferrisys.common.entity.catalog.MstUomConversion;
import java.util.List;import java.util.Optional;import java.util.UUID;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface MstUomConversionRepository extends JpaRepository<MstUomConversion, UUID> {
 Page<MstUomConversion> findByTenantIdAndDeletedAtIsNullAndGroupId(UUID tenantId, UUID groupId, Pageable pageable);
 Optional<MstUomConversion> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
 List<MstUomConversion> findByTenantIdAndGroupIdAndDeletedAtIsNull(UUID tenantId, UUID groupId);
}
