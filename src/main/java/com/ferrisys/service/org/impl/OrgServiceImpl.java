package com.ferrisys.service.org.impl;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.CreateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.CreateWarehouseRequest;
import com.ferrisys.common.dto.org.DocumentNumberingDTO;
import com.ferrisys.common.dto.org.UpdateDocumentNumberingRequest;
import com.ferrisys.common.dto.org.UpdateWarehouseRequest;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.WarehouseDTO;
import com.ferrisys.common.entity.config.DocumentType;
import com.ferrisys.common.entity.org.Branch;
import com.ferrisys.common.entity.org.DocumentNumbering;
import com.ferrisys.common.entity.org.UserBranchAssignment;
import com.ferrisys.common.entity.org.Warehouse;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.mapper.org.BranchMapper;
import com.ferrisys.mapper.org.DocumentNumberingMapper;
import com.ferrisys.mapper.org.UserBranchAssignmentMapper;
import com.ferrisys.mapper.org.WarehouseMapper;
import com.ferrisys.repository.BranchRepository;
import com.ferrisys.repository.DocumentNumberingRepository;
import com.ferrisys.repository.DocumentTypeRepository;
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
    private final DocumentNumberingMapper documentNumberingMapper;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentNumberingRepository documentNumberingRepository;
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
        entity.setDescription(dto.description());
        entity.setAddress(dto.address());
        applyBranchLocation(entity, dto);
        applyBranchContact(entity, dto);
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
        applyBranchLocation(entity, dto);
        applyBranchContact(entity, dto);
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
        return listWarehouses(branchId, Boolean.TRUE, page, size, search);
    }

    @Override
    public PageResponse<WarehouseDTO> listWarehouses(UUID branchId, Boolean active, int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        int normalizedPage = normalizePage(page);
        int responsePage = normalizeResponsePage(page);
        if (branchId != null) {
            ensureBranch(branchId, tenantId);
        }
        var p = warehouseRepository.search(tenantId, branchId, active, safeSearch(search), PageRequest.of(normalizedPage, size));
        return PageResponse.of(warehouseMapper.toDtoList(p.getContent()), p.getTotalPages(), p.getTotalElements(), responsePage, p.getSize());
    }

    @Override
    @Transactional
    public WarehouseDTO saveWarehouse(UUID branchId, WarehouseDTO dto) {
        return createWarehouse(new CreateWarehouseRequest(
                branchId,
                dto.code(),
                dto.name(),
                dto.description(),
                dto.active(),
                dto.addressLine1(),
                dto.addressLine2(),
                dto.city(),
                dto.state(),
                dto.country(),
                dto.postalCode(),
                dto.latitude(),
                dto.longitude(),
                dto.locationNotes(),
                dto.warehouseType()
        ));
    }

    @Override
    @Transactional
    public WarehouseDTO createWarehouse(CreateWarehouseRequest dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        validateWarehouse(dto.code(), dto.name());
        Branch branch = ensureBranch(dto.branchId(), tenantId);
        String code = dto.code().trim();
        if (warehouseRepository.existsByTenantIdAndBranch_IdAndCodeIgnoreCaseAndDeletedAtIsNull(tenantId, branch.getId(), code)) {
            throw new ConflictException("Ya existe una bodega con el mismo código para la sucursal");
        }
        Warehouse entity = new Warehouse();
        entity.setTenantId(tenantId);
        entity.setBranch(branch);
        entity.setCode(code);
        entity.setName(dto.name().trim());
        entity.setDescription(dto.description());
        applyWarehouseLocation(entity, dto.addressLine1(), dto.addressLine2(), dto.city(), dto.state(), dto.country(), dto.postalCode(), dto.latitude(), dto.longitude(), dto.locationNotes());
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setWarehouseType(normalizeWarehouseType(dto.warehouseType()));
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return warehouseMapper.toDto(warehouseRepository.save(entity));
    }

    @Override
    @Transactional
    public WarehouseDTO updateWarehouse(UUID id, WarehouseDTO dto) {
        return updateWarehouse(id, new UpdateWarehouseRequest(
                dto.branchId() == null ? null : UUID.fromString(dto.branchId()),
                dto.code(),
                dto.name(),
                dto.description(),
                dto.active(),
                dto.addressLine1(),
                dto.addressLine2(),
                dto.city(),
                dto.state(),
                dto.country(),
                dto.postalCode(),
                dto.latitude(),
                dto.longitude(),
                dto.locationNotes(),
                dto.warehouseType()
        ));
    }

    @Override
    @Transactional
    public WarehouseDTO updateWarehouse(UUID id, UpdateWarehouseRequest dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Warehouse entity = warehouseRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bodega no encontrada"));
        validateWarehouse(dto.code(), dto.name());
        Branch branch = dto.branchId() != null ? ensureBranch(dto.branchId(), tenantId) : entity.getBranch();
        if (warehouseRepository.existsByTenantIdAndBranch_IdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(tenantId, branch.getId(), dto.code().trim(), id)) {
            throw new ConflictException("Ya existe una bodega con el mismo código para la sucursal");
        }
        entity.setBranch(branch);
        entity.setCode(dto.code().trim());
        entity.setName(dto.name().trim());
        entity.setDescription(dto.description());
        applyWarehouseLocation(entity, dto.addressLine1(), dto.addressLine2(), dto.city(), dto.state(), dto.country(), dto.postalCode(), dto.latitude(), dto.longitude(), dto.locationNotes());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        entity.setWarehouseType(normalizeWarehouseType(dto.warehouseType()));
        return warehouseMapper.toDto(warehouseRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Warehouse entity = warehouseRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bodega no encontrada"));
        softDelete(entity);
        warehouseRepository.save(entity);
    }

    @Override
    public PageResponse<DocumentNumberingDTO> listDocumentNumbering(UUID branchId, UUID documentTypeId, Boolean active, int page, int size, String search) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        int normalizedPage = normalizePage(page);
        int responsePage = normalizeResponsePage(page);
        if (branchId != null) {
            ensureBranch(branchId, tenantId);
        }
        var result = documentNumberingRepository.search(tenantId, branchId, documentTypeId, active, safeSearch(search), PageRequest.of(normalizedPage, size));
        return PageResponse.of(documentNumberingMapper.toDtoList(result.getContent()), result.getTotalPages(), result.getTotalElements(), responsePage, result.getSize());
    }

    @Override
    @Transactional
    public DocumentNumberingDTO createDocumentNumbering(CreateDocumentNumberingRequest dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        validateNumbering(dto.series(), dto.nextNumber(), dto.padding());
        Branch branch = ensureBranch(dto.branchId(), tenantId);
        DocumentType documentType = ensureDocumentType(dto.documentTypeId(), tenantId);
        if (documentNumberingRepository.existsByTenantIdAndBranch_IdAndDocumentType_IdAndSeriesIgnoreCaseAndDeletedAtIsNull(
                tenantId, branch.getId(), documentType.getId(), dto.series().trim())) {
            throw new ConflictException("Ya existe una numeración para la sucursal/tipo/serie indicada");
        }

        DocumentNumbering entity = new DocumentNumbering();
        entity.setTenantId(tenantId);
        entity.setBranch(branch);
        entity.setDocumentType(documentType);
        entity.setSeries(dto.series().trim());
        entity.setNextNumber(dto.nextNumber());
        entity.setPadding(dto.padding() == null ? 8 : dto.padding());
        entity.setActive(dto.active() == null ? Boolean.TRUE : dto.active());
        entity.setDeletedAt(null);
        entity.setDeletedBy(null);
        return documentNumberingMapper.toDto(documentNumberingRepository.save(entity));
    }

    @Override
    @Transactional
    public DocumentNumberingDTO updateDocumentNumbering(UUID id, UpdateDocumentNumberingRequest dto) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        validateNumbering(dto.series(), dto.nextNumber(), dto.padding());
        DocumentNumbering entity = documentNumberingRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Numeración no encontrada"));
        Branch branch = dto.branchId() == null ? entity.getBranch() : ensureBranch(dto.branchId(), tenantId);
        DocumentType documentType = dto.documentTypeId() == null ? entity.getDocumentType() : ensureDocumentType(dto.documentTypeId(), tenantId);
        if (documentNumberingRepository.existsByTenantIdAndBranch_IdAndDocumentType_IdAndSeriesIgnoreCaseAndIdNotAndDeletedAtIsNull(
                tenantId, branch.getId(), documentType.getId(), dto.series().trim(), id)) {
            throw new ConflictException("Ya existe una numeración para la sucursal/tipo/serie indicada");
        }
        entity.setBranch(branch);
        entity.setDocumentType(documentType);
        entity.setSeries(dto.series().trim());
        entity.setNextNumber(dto.nextNumber());
        entity.setPadding(dto.padding() == null ? 8 : dto.padding());
        if (dto.active() != null) {
            entity.setActive(dto.active());
        }
        return documentNumberingMapper.toDto(documentNumberingRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteDocumentNumbering(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        DocumentNumbering entity = documentNumberingRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Numeración no encontrada"));
        softDelete(entity);
        documentNumberingRepository.save(entity);
    }

    @Override
    public String previewDocumentNumbering(UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        DocumentNumbering entity = documentNumberingRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Numeración no encontrada"));
        return entity.getSeries() + "-" + String.format("%0" + entity.getPadding() + "d", entity.getNextNumber());
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

    private void validateWarehouse(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("El código de la bodega es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new BadRequestException("El nombre de la bodega es obligatorio");
        }
    }

    private void validateNumbering(String series, Integer nextNumber, Integer padding) {
        if (series == null || series.isBlank()) {
            throw new BadRequestException("La serie es obligatoria");
        }
        if (nextNumber == null || nextNumber < 1) {
            throw new BadRequestException("nextNumber debe ser >= 1");
        }
        if (padding != null && (padding < 1 || padding > 20)) {
            throw new BadRequestException("padding debe estar entre 1 y 20");
        }
    }

    private DocumentType ensureDocumentType(UUID documentTypeId, UUID tenantId) {
        return documentTypeRepository.findByIdAndTenantIdAndActiveTrueAndDeletedAtIsNull(documentTypeId, tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de documento no encontrado"));
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

    private void applyBranchLocation(Branch entity, BranchDTO dto) {
        entity.setAddressLine1(dto.addressLine1());
        entity.setAddressLine2(dto.addressLine2());
        entity.setCity(dto.city());
        entity.setState(dto.state());
        entity.setCountry(dto.country());
        entity.setPostalCode(dto.postalCode());
        entity.setLatitude(dto.latitude());
        entity.setLongitude(dto.longitude());
        entity.setLocationNotes(dto.locationNotes());
    }

    private void applyWarehouseLocation(Warehouse entity,
                                        String addressLine1,
                                        String addressLine2,
                                        String city,
                                        String state,
                                        String country,
                                        String postalCode,
                                        java.math.BigDecimal latitude,
                                        java.math.BigDecimal longitude,
                                        String locationNotes) {
        entity.setAddressLine1(addressLine1);
        entity.setAddressLine2(addressLine2);
        entity.setCity(city);
        entity.setState(state);
        entity.setCountry(country);
        entity.setPostalCode(postalCode);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setLocationNotes(locationNotes);
    }

    private String normalizeWarehouseType(String warehouseType) {
        if (warehouseType == null || warehouseType.isBlank()) {
            return "MAIN";
        }
        String normalized = warehouseType.trim().toUpperCase();
        if (!Set.of("MAIN", "SALES", "RETURNS").contains(normalized)) {
            throw new BadRequestException("warehouseType debe ser MAIN, SALES o RETURNS");
        }
        return normalized;
    }

    private void applyBranchContact(Branch entity, BranchDTO dto) {
        entity.setPhone(dto.phone());
        entity.setEmail(dto.email());
        entity.setManagerName(dto.managerName());
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

    private void softDelete(DocumentNumbering entity) {
        entity.setActive(Boolean.FALSE);
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(jwtUtil.getCurrentUser());
    }
}
