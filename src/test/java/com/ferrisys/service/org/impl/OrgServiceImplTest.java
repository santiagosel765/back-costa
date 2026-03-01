package com.ferrisys.service.org.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ferrisys.common.entity.org.Branch;
import com.ferrisys.common.entity.org.UserBranchAssignment;
import com.ferrisys.common.entity.user.User;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrgServiceImplTest {

    @Mock
    private TenantContextHolder tenantContextHolder;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private UserBranchAssignmentRepository userBranchAssignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BranchMapper branchMapper;
    @Mock
    private WarehouseMapper warehouseMapper;
    @Mock
    private UserBranchAssignmentMapper userBranchAssignmentMapper;
    @Mock
    private DocumentNumberingMapper documentNumberingMapper;
    @Mock
    private DocumentTypeRepository documentTypeRepository;
    @Mock
    private DocumentNumberingRepository documentNumberingRepository;
    @Mock
    private JWTUtil jwtUtil;

    private OrgServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrgServiceImpl(tenantContextHolder, branchRepository, warehouseRepository, userBranchAssignmentRepository,
                userRepository, branchMapper, warehouseMapper, userBranchAssignmentMapper, documentNumberingMapper,
                documentTypeRepository, documentNumberingRepository, jwtUtil);
    }

    @Test
    void currentUserBranchesShouldIgnoreMissingBranchesAndKeepActiveOnes() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID activeBranchId = UUID.randomUUID();
        UUID missingBranchId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Branch activeBranch = new Branch();
        activeBranch.setId(activeBranchId);
        activeBranch.setActive(Boolean.TRUE);

        Branch missingBranchRef = new Branch();
        missingBranchRef.setId(missingBranchId);

        UserBranchAssignment activeAssignment = new UserBranchAssignment();
        activeAssignment.setBranch(activeBranch);

        UserBranchAssignment missingAssignment = new UserBranchAssignment();
        missingAssignment.setBranch(missingBranchRef);

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(jwtUtil.getCurrentUser()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userBranchAssignmentRepository.findByTenantIdAndUserIdAndActiveTrueAndDeletedAtIsNull(tenantId, userId))
                .thenReturn(List.of(activeAssignment, missingAssignment));
        when(branchRepository.findByTenantIdAndIdInAndActiveTrueAndDeletedAtIsNull(eq(tenantId), anyCollection()))
                .thenReturn(List.of(activeBranch));
        when(branchMapper.toDto(activeBranch))
                .thenReturn(sampleBranchDTO(activeBranchId.toString(), "S1001", "Sucursal 1", true));

        var result = service.currentUserBranches();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("S1001");
    }

    @Test
    void currentUserBranchesShouldReturnUnauthorizedWhenNoAuthentication() {
        when(tenantContextHolder.requireTenantId()).thenReturn(UUID.randomUUID());
        when(jwtUtil.getCurrentUser()).thenReturn("anonymousUser");

        assertThatThrownBy(() -> service.currentUserBranches())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private com.ferrisys.common.dto.org.BranchDTO sampleBranchDTO(String id, String code, String name, Boolean active) {
        return new com.ferrisys.common.dto.org.BranchDTO(
                id,
                code,
                name,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                active,
                null
        );
    }
}
