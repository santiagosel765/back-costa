package com.ferrisys.service.config.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.config.CurrencyDTO;
import com.ferrisys.common.entity.config.Currency;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.mapper.config.CurrencyMapper;
import com.ferrisys.mapper.config.DocumentTypeMapper;
import com.ferrisys.mapper.config.ParameterMapper;
import com.ferrisys.mapper.config.PaymentMethodMapper;
import com.ferrisys.mapper.config.TaxMapper;
import com.ferrisys.repository.CurrencyRepository;
import com.ferrisys.repository.DocumentTypeRepository;
import com.ferrisys.repository.ParameterRepository;
import com.ferrisys.repository.PaymentMethodRepository;
import com.ferrisys.repository.TaxRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ConfigCatalogServiceImplTest {

    @Mock private TenantContextHolder tenantContextHolder;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private TaxRepository taxRepository;
    @Mock private ParameterRepository parameterRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private DocumentTypeRepository documentTypeRepository;
    @Mock private CurrencyMapper currencyMapper;
    @Mock private TaxMapper taxMapper;
    @Mock private ParameterMapper parameterMapper;
    @Mock private PaymentMethodMapper paymentMethodMapper;
    @Mock private DocumentTypeMapper documentTypeMapper;
    @Mock private JWTUtil jwtUtil;

    private ConfigCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfigCatalogServiceImpl(
                tenantContextHolder,
                currencyRepository,
                taxRepository,
                parameterRepository,
                paymentMethodRepository,
                documentTypeRepository,
                currencyMapper,
                taxMapper,
                parameterMapper,
                paymentMethodMapper,
                documentTypeMapper,
                jwtUtil
        );
    }

    @Test
    void listCurrenciesShouldReturnPagedDataWithOneBasedPage() {
        UUID tenantId = UUID.randomUUID();
        Currency currency = new Currency();
        currency.setCode("USD");

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(currencyRepository.searchByTenant(eq(tenantId), eq(""), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(currency), PageRequest.of(0, 10), 1));
        when(currencyMapper.toDtoList(any())).thenReturn(List.of(new CurrencyDTO(null, "USD", "Dollar", null, "$", 2, false, null, true, null)));

        PageResponse<CurrencyDTO> result = service.listCurrencies(1, 10, "");

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void saveCurrencyShouldClearOtherFunctionalCurrenciesWhenRequested() {
        UUID tenantId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();

        Currency input = new Currency();
        input.setCode("usd");
        input.setName("Dollar");
        input.setIsFunctional(true);

        Currency saved = new Currency();
        saved.setId(createdId);
        saved.setCode("USD");
        saved.setName("Dollar");
        saved.setIsFunctional(true);

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(currencyMapper.toEntity(any())).thenReturn(input);
        when(currencyRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, "USD")).thenReturn(Optional.empty());
        when(currencyRepository.save(any())).thenReturn(saved);
        when(currencyMapper.toDto(saved)).thenReturn(new CurrencyDTO(createdId.toString(), "USD", "Dollar", null, "$", 2, true, null, true, null));

        CurrencyDTO result = service.saveCurrency(new CurrencyDTO(null, "usd", "Dollar", null, "$", 2, true, null, true, null));

        assertThat(result.isFunctional()).isTrue();
        verify(currencyRepository).unsetFunctionalCurrencies(eq(tenantId), any());
    }

    @Test
    void updateCurrencyShouldThrowConflictWhenCodeExists() {
        UUID tenantId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();

        Currency current = new Currency();
        current.setId(targetId);
        current.setCode("EUR");
        current.setName("Euro");

        Currency duplicated = new Currency();
        duplicated.setId(existingId);

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(currencyRepository.findByIdAndTenantIdAndDeletedAtIsNull(targetId, tenantId)).thenReturn(Optional.of(current));
        when(currencyRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, "USD")).thenReturn(Optional.of(duplicated));

        assertThatThrownBy(() -> service.updateCurrency(targetId, new CurrencyDTO(null, "usd", "Dollar", null, "$", 2, false, null, true, null)))
                .isInstanceOf(ConflictException.class);

        verify(currencyRepository, never()).save(any());
    }

    @Test
    void setFunctionalCurrencyShouldClearOtherCurrencies() {
        UUID tenantId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        Currency current = new Currency();
        current.setId(targetId);

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(currencyRepository.findByIdAndTenantIdAndDeletedAtIsNull(targetId, tenantId)).thenReturn(Optional.of(current));
        when(currencyRepository.setFunctionalCurrency(tenantId, targetId)).thenReturn(1);
        when(currencyMapper.toDto(current)).thenReturn(new CurrencyDTO(targetId.toString(), "USD", "Dollar", null, "$", 2, true, null, true, null));

        CurrencyDTO result = service.setFunctionalCurrency(targetId);

        assertThat(result.isFunctional()).isTrue();
        verify(currencyRepository).unsetFunctionalCurrencies(tenantId, targetId);
    }
}
