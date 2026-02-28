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
        var p = currencyRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(page, size));
        return PageResponse.of(currencyMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public CurrencyDTO saveCurrency(CurrencyDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Currency entity = currencyMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return currencyMapper.toDto(currencyRepository.save(entity));
    }

    @Override
    @Transactional
    public CurrencyDTO updateCurrency(UUID id, CurrencyDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Currency entity = currencyRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Moneda no encontrada"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return currencyMapper.toDto(currencyRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteCurrency(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Currency entity = currencyRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Moneda no encontrada"));
        softDelete(entity);
        currencyRepository.save(entity);
    }

    @Override
    public PageResponse<TaxDTO> listTaxes(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = taxRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(page, size));
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
                tenantId, safeSearch(search), PageRequest.of(page, size));
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
                tenantId, safeSearch(search), PageRequest.of(page, size));
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
                tenantId, safeSearch(search), PageRequest.of(page, size));
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

    private String safeSearch(String search) {
        return search == null ? "" : search;
    }

    private void softDelete(Currency entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(Tax entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(Parameter entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(PaymentMethod entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
    private void softDelete(DocumentType entity) { entity.setActive(Boolean.FALSE); entity.setDeletedAt(OffsetDateTime.now()); entity.setDeletedBy(jwtUtil.getCurrentUser()); }
}
