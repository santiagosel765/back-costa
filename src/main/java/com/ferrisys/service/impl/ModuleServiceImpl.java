package com.ferrisys.service.impl;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.mapper.ModuleMapper;
import com.ferrisys.repository.ModuleRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final TenantContextHolder tenantContextHolder;

    @Transactional
    public void saveOrUpdate(ModuleDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        AuthModule module = moduleMapper.toEntity(dto);
        if (module.getId() != null && moduleRepository.findByIdAndTenantId(module.getId(), tenantId).isEmpty()) {
            throw new NotFoundException("Módulo no encontrado");
        }
        module.setTenantId(tenantId);
        moduleRepository.save(module);
    }

    public PageResponse<ModuleDTO> getAll(int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Page<ModuleDTO> pageDto = moduleRepository.findByTenantId(tenantId, PageRequest.of(page, size))
                .map(moduleMapper::toDto);
        return PageResponse.from(pageDto);
    }

    public void disableModule(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        AuthModule module = moduleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Módulo no encontrado"));
        module.setStatus(0);
        moduleRepository.save(module);
    }
}
