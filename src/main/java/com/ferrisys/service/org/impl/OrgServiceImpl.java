package com.ferrisys.service.org.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.WarehouseDTO;
import com.ferrisys.common.entity.org.Branch;
import com.ferrisys.common.entity.org.UserBranchAssignment;
import com.ferrisys.common.entity.org.Warehouse;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.mapper.org.BranchMapper;
import com.ferrisys.mapper.org.UserBranchAssignmentMapper;
import com.ferrisys.mapper.org.WarehouseMapper;
import com.ferrisys.repository.BranchRepository;
import com.ferrisys.repository.UserBranchAssignmentRepository;
import com.ferrisys.repository.UserRepository;
import com.ferrisys.repository.WarehouseRepository;
import com.ferrisys.service.org.OrgService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final TenantContextHolder tenantContextHolder;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserBranchAssignmentRepository userBranchAssignmentRepository;
    private final UserRepository userRepository;
    private final BranchMapper branchMapper;
    private final WarehouseMapper warehouseMapper;
    private final UserBranchAssignmentMapper userBranchAssignmentMapper;
    private final JWTUtil jwtUtil;

    @Override
    public PageResponse<BranchDTO> listBranches(int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        var p = branchRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(page, size));
        return PageResponse.of(branchMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public BranchDTO saveBranch(BranchDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Branch entity = branchMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return branchMapper.toDto(branchRepository.save(entity));
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(UUID id, BranchDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Branch entity = branchRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setAddress(dto.address());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return branchMapper.toDto(branchRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Branch entity = branchRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        softDelete(entity);
        branchRepository.save(entity);
    }

    @Override
    public PageResponse<WarehouseDTO> listWarehouses(UUID branchId, int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        ensureBranch(branchId, tenantId);
        var p = warehouseRepository.findByTenantIdAndBranch_IdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, branchId, safeSearch(search), PageRequest.of(page, size));
        return PageResponse.of(warehouseMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @Override
    @Transactional
    public WarehouseDTO saveWarehouse(UUID branchId, WarehouseDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Branch branch = ensureBranch(branchId, tenantId);
        Warehouse entity = new Warehouse();
        entity.setTenantId(tenantId);
        entity.setBranch(branch);
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return warehouseMapper.toDto(warehouseRepository.save(entity));
    }

    @Override
    @Transactional
    public WarehouseDTO updateWarehouse(UUID id, WarehouseDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Warehouse entity = warehouseRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bodega no encontrada"));
        if (dto.branchId() != null) {
            entity.setBranch(ensureBranch(UUID.fromString(dto.branchId()), tenantId));
        }
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return warehouseMapper.toDto(warehouseRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Warehouse entity = warehouseRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bodega no encontrada"));
        softDelete(entity);
        warehouseRepository.save(entity);
    }

    @Override
    public PageResponse<UserBranchAssignmentDTO> listUserBranchAssignments(UUID userId, UUID branchId, int page, int size) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Page<UserBranchAssignment> assignments;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (userId != null && branchId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(
                    tenantId, userId, branchId, pageRequest);
        } else if (userId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(tenantId, userId, pageRequest);
        } else if (branchId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndBranch_IdAndDeletedAtIsNull(tenantId, branchId, pageRequest);
        } else {
            throw new BadRequestException("Debe enviar userId o branchId");
        }

        return PageResponse.of(userBranchAssignmentMapper.toDtoList(assignments.getContent()),
                assignments.getTotalPages(), assignments.getTotalElements(), assignments.getNumber(), assignments.getSize());
    }

    @Override
    @Transactional
    public UserBranchAssignmentDTO createUserBranchAssignment(UUID userId, UUID branchId) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        ensureBranch(branchId, tenantId);

        UserBranchAssignment existingActive = userBranchAssignmentRepository
                .findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(tenantId, userId, branchId)
                .orElse(null);
        if (existingActive != null && Boolean.TRUE.equals(existingActive.getActive())) {
            throw new ConflictException("La asignación ya existe");
        }

        UserBranchAssignment assignment = userBranchAssignmentRepository
                .findByTenantIdAndUserIdAndBranch_Id(tenantId, userId, branchId)
                .orElseGet(UserBranchAssignment::new);

        assignment.setTenantId(tenantId);
        assignment.setUserId(userId);
        assignment.setBranch(ensureBranch(branchId, tenantId));
        assignment.setActive(Boolean.TRUE);
        assignment.setDeletedAt(null);
        assignment.setDeletedBy(null);
        return userBranchAssignmentMapper.toDto(userBranchAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteUserBranchAssignment(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        UserBranchAssignment entity = userBranchAssignmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Asignación no encontrada"));
        softDelete(entity);
        userBranchAssignmentRepository.save(entity);
    }

    @Override
    public List<BranchDTO> currentUserBranches() {
        UUID tenantId = tenantContextHolder.requireTenantId();
        String username = jwtUtil.getCurrentUser();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        List<UserBranchAssignment> assignments = userBranchAssignmentRepository
                .findByTenantIdAndUserIdAndActiveTrueAndDeletedAtIsNull(tenantId, user.getId());

        return assignments.stream()
                .map(UserBranchAssignment::getBranch)
                .filter(branch -> Boolean.TRUE.equals(branch.getActive()) && branch.getDeletedAt() == null)
                .map(branchMapper::toDto)
                .toList();
    }

    private Branch ensureBranch(UUID branchId, UUID tenantId) {
        return branchRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(branchId, tenantId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
    }

    private String safeSearch(String search) {
        return search == null ? "" : search;
    }

    private void softDelete(Branch entity) {
        entity.setActive(Boolean.FALSE);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(jwtUtil.getCurrentUser());
    }

    private void softDelete(Warehouse entity) {
        entity.setActive(Boolean.FALSE);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(jwtUtil.getCurrentUser());
    }

    private void softDelete(UserBranchAssignment entity) {
        entity.setActive(Boolean.FALSE);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(jwtUtil.getCurrentUser());
    }
}
