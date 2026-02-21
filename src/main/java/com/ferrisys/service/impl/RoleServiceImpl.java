package com.ferrisys.service.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RoleDTO;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthRoleModule;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.repository.ModuleRepository;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.repository.RoleRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleRepository roleModuleRepository;

    @Transactional
    public void saveOrUpdate(RoleDTO dto) {
        Role role;
        if (dto.getId() != null) {
            role = roleRepository.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
            role.setName(dto.getName());
            role.setDescription(dto.getDescription());
            role.setStatus(dto.getStatus());
        } else {
            role = Role.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .status(dto.getStatus() != null ? dto.getStatus() : 1)
                    .build();
        }

        Role saved = roleRepository.save(role);

        roleModuleRepository.deleteByRole(saved);
        if (dto.getModuleIds() != null) {
            for (UUID moduleId : dto.getModuleIds()) {
                AuthModule module = moduleRepository.findById(moduleId)
                        .orElseThrow(() -> new NotFoundException("Módulo no encontrado"));
                roleModuleRepository.save(AuthRoleModule.builder()
                        .role(saved)
                        .module(module)
                        .status(1)
                        .build());
            }
        }
    }

    public PageResponse<RoleDTO> getAll(int page, int size) {
        Page<Role> result = roleRepository.findAll(PageRequest.of(page, size));
        List<RoleDTO> content = result.getContent().stream()
                .map(role -> RoleDTO.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .status(role.getStatus())
                        .build())
                .toList();
        return new PageResponse<>(content, result.getTotalPages(), result.getTotalElements(),
                result.getNumber(), result.getSize());
    }

    @Transactional
    public void disableRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
        role.setStatus(0);
        roleRepository.save(role);
    }
}
