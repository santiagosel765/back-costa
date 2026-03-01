package com.ferrisys.repository.catalog;
import com.ferrisys.common.entity.catalog.MstUom;
import java.util.List;import java.util.Optional;import java.util.UUID;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface MstUomRepository extends JpaRepository<MstUom, UUID> {
 Page<MstUom> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId,String search,Boolean active, Pageable pageable);
 Page<MstUom> findByTenantIdAndDeletedAtIsNullAndGroupId(UUID tenantId, UUID groupId, Pageable pageable);
 Optional<MstUom> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
 List<MstUom> findByTenantIdAndGroupIdAndDeletedAtIsNullAndActiveTrue(UUID tenantId, UUID groupId);
}
