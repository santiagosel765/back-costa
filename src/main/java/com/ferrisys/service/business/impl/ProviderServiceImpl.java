package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;
import com.ferrisys.common.entity.business.Provider;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.mapper.ProviderMapper;
import com.ferrisys.repository.ProviderRepository;
import com.ferrisys.service.business.ProviderService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;
    private final TenantContextHolder tenantContextHolder;

    @Override
    @Transactional
    public ProviderDTO saveOrUpdate(ProviderDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Provider entity = providerMapper.toEntity(dto);
        if (entity.getId() != null) {
            Provider existing = providerRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(entity.getId(), tenantId)
                    .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
            entity.setCreatedAt(existing.getCreatedAt());
        }
        entity.setTenantId(tenantId);
        entity.setActive(Boolean.TRUE);
        entity.setDeletedAt(null);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        return providerMapper.toDto(providerRepository.save(entity));
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Provider provider = providerRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        provider.setStatus(0);
        provider.setActive(Boolean.FALSE);
        provider.setDeletedAt(OffsetDateTime.now());
        providerRepository.save(provider);
    }

    @Override
    public PageResponse<ProviderDTO> list(int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Page<ProviderDTO> pageDto = providerRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId, PageRequest.of(page, size))
                .map(providerMapper::toDto);
        return PageResponse.from(pageDto);
    }

    @Override
    public ProviderDTO getById(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Provider provider = providerRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        return providerMapper.toDto(provider);
    }
}
