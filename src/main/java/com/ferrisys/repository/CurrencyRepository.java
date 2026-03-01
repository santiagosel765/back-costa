package com.ferrisys.repository;

import com.ferrisys.common.entity.config.Currency;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurrencyRepository extends JpaRepository<Currency, UUID> {
    Optional<Currency> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    @Query("""
            select c
              from Currency c
             where c.tenantId = :tenantId
               and c.deletedAt is null
               and (
                    lower(c.code) like lower(concat('%', :search, '%'))
                    or lower(c.name) like lower(concat('%', :search, '%'))
               )
            """)
    Page<Currency> searchByTenant(@Param("tenantId") UUID tenantId, @Param("search") String search, Pageable pageable);

    Optional<Currency> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Currency c
               set c.isFunctional = false,
                   c.updatedAt = current_timestamp
             where c.tenantId = :tenantId
               and c.isFunctional = true
               and c.deletedAt is null
               and c.id <> :excludeId
            """)
    int unsetFunctionalCurrencies(@Param("tenantId") UUID tenantId, @Param("excludeId") UUID excludeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Currency c
               set c.isFunctional = true,
                   c.updatedAt = current_timestamp
             where c.tenantId = :tenantId
               and c.id = :id
               and c.deletedAt is null
            """)
    int setFunctionalCurrency(@Param("tenantId") UUID tenantId, @Param("id") UUID id);

    boolean existsByTenantIdAndIsFunctionalTrueAndDeletedAtIsNullAndIdNot(UUID tenantId, UUID id);
}
