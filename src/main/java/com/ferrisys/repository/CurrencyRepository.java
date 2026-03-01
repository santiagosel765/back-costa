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

    @Modifying
    @Query("""
            update Currency c
               set c.isFunctional = false
             where c.tenantId = :tenantId
               and c.deletedAt is null
               and c.id <> :currencyId
            """)
    void clearFunctionalForTenant(@Param("tenantId") UUID tenantId, @Param("currencyId") UUID currencyId);
}
