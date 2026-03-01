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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
        int normalizedPage = normalizePage(page);
        int responsePage = normalizeResponsePage(page);
        var p = branchRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, safeSearch(search), PageRequest.of(normalizedPage, size));
        return PageResponse.of(branchMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), responsePage, p.getSize());
    }

    @Override
    @Transactional
    public BranchDTO saveBranch(BranchDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        validateBranch(dto.code(), dto.name());
        if (branchRepository.existsByTenantIdAndCodeIgnoreCaseAndActiveTrueAndDeletedAtIsNull(tenantId, dto.code().trim())) {
            throw new ConflictException("Ya existe una sucursal con el mismo código");
        }
        Branch entity = branchMapper.toEntity(dto);
        entity.setId(null);
        entity.setTenantId(tenantId);
        entity.setCode(dto.code().trim());
        entity.setName(dto.name().trim());
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
        validateBranch(dto.code(), dto.name());
        if (branchRepository.existsByTenantIdAndCodeIgnoreCaseAndIdNotAndActiveTrueAndDeletedAtIsNull(tenantId, dto.code().trim(), id)) {
            throw new ConflictException("Ya existe una sucursal con el mismo código");
        }
        entity.setCode(dto.code().trim());
        entity.setName(dto.name().trim());
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
        int normalizedPage = normalizePage(page);
        int responsePage = normalizeResponsePage(page);
        ensureBranch(branchId, tenantId);
        var p = warehouseRepository.findByTenantIdAndBranch_IdAndActiveTrueAndDeletedAtIsNullAndNameContainingIgnoreCase(
                tenantId, branchId, safeSearch(search), PageRequest.of(normalizedPage, size));
        return PageResponse.of(warehouseMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), responsePage, p.getSize());
    }

    @Override
    @Transactional
    public WarehouseDTO saveWarehouse(UUID branchId, WarehouseDTO dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Branch branch = ensureBranch(branchId, tenantId);
        Warehouse entity = new Warehouse();
        entity.setTenantId(tenantId);
        entity.setBranch(branch);
        entity.setCode(dto.code().trim());
        entity.setName(dto.name().trim());
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
        entity.setCode(dto.code().trim());
        entity.setName(dto.name().trim());
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
        int responsePage = normalizeResponsePage(page);
        PageRequest pageRequest = PageRequest.of(normalizePage(page), size);

        if (userId != null && branchId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(
                    tenantId, userId, branchId, pageRequest);
        } else if (userId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(tenantId, userId, pageRequest);
        } else if (branchId != null) {
            assignments = userBranchAssignmentRepository.findByTenantIdAndBranch_IdAndDeletedAtIsNull(tenantId, branchId, pageRequest);
        } else {
            assignments = userBranchAssignmentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageRequest);
        }

        return PageResponse.of(userBranchAssignmentMapper.toDtoList(assignments.getContent()),
                assignments.getTotalPages(), assignments.getTotalElements(), responsePage, assignments.getSize());
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
        User user = getCurrentUser();

        try {
            List<UserBranchAssignment> assignments = userBranchAssignmentRepository
                    .findByTenantIdAndUserIdAndActiveTrueAndDeletedAtIsNull(tenantId, user.getId());

            Set<UUID> branchIds = assignments.stream()
                    .map(UserBranchAssignment::getBranch)
                    .filter(java.util.Objects::nonNull)
                    .map(Branch::getId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (branchIds.isEmpty()) {
                return List.of();
            }

            Map<UUID, Branch> branchesById = branchRepository
                    .findByTenantIdAndIdInAndActiveTrueAndDeletedAtIsNull(tenantId, branchIds)
                    .stream()
                    .collect(Collectors.toMap(Branch::getId, Function.identity()));

            return assignments.stream()
                    .map(UserBranchAssignment::getBranch)
                    .filter(java.util.Objects::nonNull)
                    .map(Branch::getId)
                    .filter(java.util.Objects::nonNull)
                    .map(branchId -> mapAssignedBranch(branchId, branchesById, tenantId, user.getId()))
                    .filter(java.util.Objects::nonNull)
                    .map(branchMapper::toDto)
                    .toList();
        } catch (RuntimeException ex) {
            log.error("Error retrieving branches for user {} in tenant {}", user.getId(), tenantId, ex);
            throw ex;
        }
    }

    @Override
    public BranchDTO currentUserBranch() {
        return currentUserBranches().stream().findFirst().orElse(null);
    }

    @Override
    public void validateCurrentUserBranch(UUID branchId) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        User user = getCurrentUser();

        ensureBranch(branchId, tenantId);

        boolean assigned = userBranchAssignmentRepository
                .findByTenantIdAndUserIdAndBranch_IdAndDeletedAtIsNull(tenantId, user.getId(), branchId)
                .filter(assignment -> Boolean.TRUE.equals(assignment.getActive()))
                .isPresent();

        if (!assigned) {
            throw new AccessDeniedException("No tiene acceso a la sucursal indicada");
        }
    }

    private User getCurrentUser() {
        String username = null;
        try {
            username = jwtUtil.getCurrentUser();
        } catch (RuntimeException ex) {
            log.warn("Unable to resolve current authenticated user from security context", ex);
        }

        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        final String resolvedUsername = username;
        return userRepository.findByUsername(resolvedUsername)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuario autenticado no encontrado"));
    }

    private Branch mapAssignedBranch(UUID branchId, Map<UUID, Branch> branchesById, UUID tenantId, UUID userId) {
        Branch branch = branchesById.get(branchId);
        if (branch == null) {
            log.warn("Ignoring assignment to missing or inactive branch {} for user {} in tenant {}", branchId, userId, tenantId);
        }
        return branch;
    }

    private Branch ensureBranch(UUID branchId, UUID tenantId) {
        return branchRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(branchId, tenantId)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
    }

    private String safeSearch(String search) {
        return search == null ? "" : search;
    }

    private int normalizePage(int page) {
        return normalizeResponsePage(page) - 1;
    }

    private int normalizeResponsePage(int page) {
        return Math.max(page, 1);
    }

    private void validateBranch(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("El código de la sucursal es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new BadRequestException("El nombre de la sucursal es obligatorio");
        }
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
