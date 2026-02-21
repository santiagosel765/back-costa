package com.ferrisys.service.business.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;
import com.ferrisys.common.entity.business.Provider;
import com.ferrisys.mapper.ProviderMapper;
import com.ferrisys.repository.ProviderRepository;
import com.ferrisys.service.business.ProviderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    @Override
    @Transactional
    public void saveOrUpdate(ProviderDTO dto) {
        providerRepository.save(providerMapper.toEntity(dto));
    }

    @Override
    @Transactional
    public void disable(UUID id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        provider.setStatus(0);
        providerRepository.save(provider);
    }

    @Override
    public PageResponse<ProviderDTO> list(int page, int size) {
        Page<ProviderDTO> pageDto = providerRepository.findAll(PageRequest.of(page, size))
                .map(providerMapper::toDto);
        return PageResponse.from(pageDto);
    }
}
