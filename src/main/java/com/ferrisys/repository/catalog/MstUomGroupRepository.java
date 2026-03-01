package com.ferrisys.repository.catalog;
import com.ferrisys.common.entity.catalog.MstUomGroup;
import java.util.Optional;import java.util.UUID;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;
public interface MstUomGroupRepository extends JpaRepository<MstUomGroup, UUID> {
 Page<MstUomGroup> findByTenantIdAndDeletedAtIsNullAndNameContainingIgnoreCaseAndActive(UUID tenantId,String search,Boolean active, Pageable pageable);
 Optional<MstUomGroup> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
