package com.ferrisys.service.impl;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.mapper.ModuleMapper;
import com.ferrisys.repository.ModuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;

    @Transactional
    public void saveOrUpdate(ModuleDTO dto) {
        AuthModule module = moduleMapper.toEntity(dto);
        if (module.getId() != null && !moduleRepository.existsById(module.getId())) {
            throw new RuntimeException("Módulo no encontrado");
        }
        moduleRepository.save(module);
    }

    public PageResponse<ModuleDTO> getAll(int page, int size) {
        Page<ModuleDTO> pageDto = moduleRepository.findAll(PageRequest.of(page, size))
                .map(moduleMapper::toDto);
        return PageResponse.from(pageDto);
    }

    public void disableModule(UUID id) {
        AuthModule module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Módulo no encontrado"));
        module.setStatus(0);
        moduleRepository.save(module);
    }
}
