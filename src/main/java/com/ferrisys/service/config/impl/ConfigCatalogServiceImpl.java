package com.ferrisys.service.config.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.config.CurrencyDTO;
import com.ferrisys.common.dto.config.DocumentTypeDTO;
import com.ferrisys.common.dto.config.ParameterDTO;
import com.ferrisys.common.dto.config.PaymentMethodDTO;
import com.ferrisys.common.dto.config.TaxDTO;
import com.ferrisys.common.entity.config.Currency;
import com.ferrisys.common.entity.config.DocumentType;
import com.ferrisys.common.entity.config.Parameter;
import com.ferrisys.common.entity.config.PaymentMethod;
import com.ferrisys.common.entity.config.Tax;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.common.exception.impl.NotFoundException;
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
import com.ferrisys.service.config.ConfigCatalogService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigCatalogServiceImpl implements ConfigCatalogService {

    private final TenantContextHolder tenantContextHolder;
    private final CurrencyRepository currencyRepository;
    private final TaxRepository taxRepository;
    private final ParameterRepository parameterRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final CurrencyMapper currencyMapper;
    private final TaxMapper taxMapper;
    private final ParameterMapper parameterMapper;
    private final PaymentMethodMapper paymentMethodMapper;
    private final DocumentTypeMapper documentTypeMapper;
    private final JWTUtil jwtUtil;

    @Override
    public PageResponse<CurrencyDTO> listCurrencies(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = currencyRepository.searchByTenant(
                tenantId, safeSearch(search), PageRequest.of(normalizePage(page), size));
        return PageResponse.of(currencyMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber() + 1, p.getSize());
    }

    @Override
    @Transactional
    public CurrencyDTO saveCurrency(CurrencyDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        try {
            Currency entity = currencyMapper.toEntity(dto);
            entity.setId(null);
            entity.setTenantId(tenantId);
            validateAndNormalizeCurrency(tenantId, entity, null);
            entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
            entity.setDecimals(dto.decimals() == null ? 2 : dto.decimals());
            boolean isFunctional = dto.isFunctional() != null && dto.isFunctional();
            if (isFunctional) {
                currencyRepository.unsetFunctionalCurrencies(tenantId, UUID.randomUUID());
            }
            entity.setIsFunctional(isFunctional);
            entity.setDeletedAt(null);
            entity.setDeletedBy(null);
            Currency saved = currencyRepository.save(entity);
            return currencyMapper.toDto(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ya existe una moneda funcional para este tenant.");
        }
    }

    @Override
    @Transactional
    public CurrencyDTO updateCurrency(UUID id, CurrencyDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        try {
            Currency entity = currencyRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                    .orElseThrow(() -> new NotFoundException("Moneda no encontrada"));
            entity.setCode(dto.code());
            entity.setName(dto.name());
            entity.setDescription(dto.description());
            entity.setSymbol(dto.symbol());
            entity.setDecimals(dto.decimals() == null ? entity.getDecimals() : dto.decimals());
            entity.setExchangeRateRef(dto.exchangeRateRef());
            boolean isFunctional = dto.isFunctional() != null && dto.isFunctional();
            if (isFunctional) {
                currencyRepository.unsetFunctionalCurrencies(tenantId, id);
            }
            entity.setIsFunctional(isFunctional);
            validateAndNormalizeCurrency(tenantId, entity, id);
            if (dto.active() != null) {
                entity.setActive(dto.active());
            }
            Currency saved = currencyRepository.save(entity);
            return currencyMapper.toDto(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ya existe una moneda funcional para este tenant.");
        }
    }

    @Override
    @Transactional
    public CurrencyDTO setFunctionalCurrency(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        try {
            Currency entity = currencyRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                    .orElseThrow(() -> new NotFoundException("Moneda no encontrada"));
            currencyRepository.unsetFunctionalCurrencies(tenantId, id);
            int updatedRows = currencyRepository.setFunctionalCurrency(tenantId, id);
            if (updatedRows == 0) {
                throw new NotFoundException("Moneda no encontrada");
            }
            entity.setIsFunctional(Boolean.TRUE);
            return currencyMapper.toDto(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ya existe una moneda funcional para este tenant.");
        }
    }

    @Override
    @Transactional
    public void deleteCurrency(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Currency entity = currencyRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Moneda no encontrada"));
        if (Boolean.TRUE.equals(entity.getIsFunctional())
                && !currencyRepository.existsByTenantIdAndIsFunctionalTrueAndDeletedAtIsNullAndIdNot(tenantId, id)) {
            throw new BadRequestException("No puedes eliminar la moneda funcional sin definir otra funcional antes.");
        }
        entity.setIsFunctional(Boolean.FALSE);
        softDelete(entity);
        currencyRepository.save(entity);
    }

    @Override
    public PageResponse<TaxDTO> listTaxes(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = taxRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(normalizePage(page), size));
        return PageResponse.of(taxMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public TaxDTO saveTax(TaxDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Tax entity = taxMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return taxMapper.toDto(taxRepository.save(entity));
    }

    @Override
    @Transactional
    public TaxDTO updateTax(UUID id, TaxDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Tax entity = taxRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Impuesto no encontrado"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setRate(dto.rate());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return taxMapper.toDto(taxRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteTax(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Tax entity = taxRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Impuesto no encontrado"));
        softDelete(entity);
        taxRepository.save(entity);
    }

    @Override
    public PageResponse<ParameterDTO> listParameters(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = parameterRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(normalizePage(page), size));
        return PageResponse.of(parameterMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public ParameterDTO saveParameter(ParameterDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Parameter entity = parameterMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return parameterMapper.toDto(parameterRepository.save(entity));
    }

    @Override
    @Transactional
    public ParameterDTO updateParameter(UUID id, ParameterDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Parameter entity = parameterRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Parámetro no encontrado"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setValue(dto.value());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return parameterMapper.toDto(parameterRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteParameter(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Parameter entity = parameterRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Parámetro no encontrado"));
        softDelete(entity);
        parameterRepository.save(entity);
    }

    @Override
    public PageResponse<PaymentMethodDTO> listPaymentMethods(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = paymentMethodRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(normalizePage(page), size));
        return PageResponse.of(paymentMethodMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public PaymentMethodDTO savePaymentMethod(PaymentMethodDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        PaymentMethod entity = paymentMethodMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return paymentMethodMapper.toDto(paymentMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public PaymentMethodDTO updatePaymentMethod(UUID id, PaymentMethodDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        PaymentMethod entity = paymentMethodRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return paymentMethodMapper.toDto(paymentMethodRepository.save(entity));
    }

    @Override
    @Transactional
    public void deletePaymentMethod(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        PaymentMethod entity = paymentMethodRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Método de pago no encontrado"));
        softDelete(entity);
        paymentMethodRepository.save(entity);
    }

    @Override
    public PageResponse<DocumentTypeDTO> listDocumentTypes(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = documentTypeRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(normalizePage(page), size));
        return PageResponse.of(documentTypeMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public DocumentTypeDTO saveDocumentType(DocumentTypeDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        DocumentType entity = documentTypeMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return documentTypeMapper.toDto(documentTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTypeDTO updateDocumentType(UUID id, DocumentTypeDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        DocumentType entity = documentTypeRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de documento no encontrado"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return documentTypeMapper.toDto(documentTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteDocumentType(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        DocumentType entity = documentTypeRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de documento no encontrado"));
        softDelete(entity);
        documentTypeRepository.save(entity);
    }


    private int normalizePage(int page) {
        return page <= 1 ? 0 : page - 1;
    }

    private String safeSearch(String search) {
        return search == null ? "" : search;
    }

    private void validateAndNormalizeCurrency(UUID tenantId, Currency entity, UUID currentId) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            throw new BadRequestException("code es requerido");
        }
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new BadRequestException("name es requerido");
        }
        String normalizedCode = entity.getCode().trim().toUpperCase();
        entity.setCode(normalizedCode);
        entity.setName(entity.getName().trim());
        if (entity.getDecimals() == null) {
            entity.setDecimals(2);
        }
        if (currencyRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, normalizedCode)
                .filter(found -> currentId == null || !found.getId().equals(currentId))
                .isPresent()) {
            throw new ConflictException("Ya existe una moneda con el mismo code");
        }
    }

    private void softDelete(Currency entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(Tax entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(Parameter entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(PaymentMethod entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(DocumentType entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
}
