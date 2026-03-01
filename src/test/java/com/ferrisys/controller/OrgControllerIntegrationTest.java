package com.ferrisys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
import com.ferrisys.common.dto.org.UserBranchAssignmentEnrichedDTO;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.config.license.ModuleLicenseInterceptor;
import com.ferrisys.config.license.ModuleLicenseService;
import com.ferrisys.config.license.ModuleResolver;
import com.ferrisys.config.security.CustomUserDetailsService;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.config.web.GlobalExceptionHandler;
import com.ferrisys.config.web.ModuleGuardWebConfig;
import com.ferrisys.core.tenant.TenantContext;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.core.tenant.TenantResolver;
import com.ferrisys.service.org.OrgService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = OrgController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
        ModuleGuardWebConfig.class,
        ModuleLicenseInterceptor.class,
        ModuleResolver.class
})
@ImportAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
class OrgControllerIntegrationTest {

    @MockBean
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrgService orgService;

    @MockBean
    private ModuleLicenseService moduleLicenseService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private TenantResolver tenantResolver;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(UUID.randomUUID().toString());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldListBranches() throws Exception {
        when(orgService.listBranches(1, 10, ""))
                .thenReturn(new PageResponse<>(List.of(branchDto("MTR", "Matriz", true)), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/org/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Matriz"));
    }



    @Test
    void shouldCreateBranchWithCreatedStatus() throws Exception {
        when(orgService.saveBranch(any()))
                .thenReturn(branchDto("MTR", "Matriz", true));

        mockMvc.perform(post("/v1/org/branches")
                        .contentType("application/json")
                        .content("{\"code\":\"MTR\",\"name\":\"Matriz\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("MTR"));
    }



    @Test
    void shouldAllowZeroPageAndReturnNormalizedPage() throws Exception {
        when(orgService.listBranches(0, 10, ""))
                .thenReturn(new PageResponse<>(List.of(branchDto("MTR", "Matriz", true)), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/org/branches").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void shouldReturn404WhenBranchNotFound() throws Exception {
        when(orgService.updateBranch(any(), any())).thenThrow(new NotFoundException("Sucursal no encontrada"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/org/branches/{id}", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{\"code\":\"MTR\",\"name\":\"Matriz\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void shouldListAssignmentsByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(orgService.listUserBranchAssignments(eq(userId), eq(null), eq(1), eq(10)))
                .thenReturn(new PageResponse<>(List.of(new UserBranchAssignmentDTO(UUID.randomUUID().toString(), userId.toString(), UUID.randomUUID().toString(), true, null)), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/org/user-branch-assignments").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()));
    }

    @Test
    void shouldCreateAssignment() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        when(orgService.createUserBranchAssignment(userId, branchId))
                .thenReturn(new UserBranchAssignmentDTO(UUID.randomUUID().toString(), userId.toString(), branchId.toString(), true, null));

        mockMvc.perform(post("/v1/org/user-branch-assignments")
                        .contentType("application/json")
                        .content("{\"userId\":\"" + userId + "\",\"branchId\":\"" + branchId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    void shouldListAssignmentsWithoutFilters() throws Exception {
        when(orgService.listUserBranchAssignments(eq(null), eq(null), eq(1), eq(10)))
                .thenReturn(new PageResponse<>(List.of(new UserBranchAssignmentDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), true, null)), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/org/user-branch-assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldListEnrichedAssignments() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        when(orgService.listUserBranchAssignmentsEnriched(eq(userId), eq(branchId), eq(1), eq(10)))
                .thenReturn(new PageResponse<>(List.of(new UserBranchAssignmentEnrichedDTO(
                        UUID.randomUUID().toString(),
                        userId.toString(),
                        "Jane Doe",
                        "jane@ferrisys.com",
                        branchId.toString(),
                        "SCL",
                        "Sucursal Centro",
                        true,
                        null
                )), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/org/user-branch-assignments/enriched")
                        .param("userId", userId.toString())
                        .param("branchId", branchId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userFullName").value("Jane Doe"))
                .andExpect(jsonPath("$.data[0].branchCode").value("SCL"));
    }

    @Test
    void shouldListCurrentUserBranchesInMeEndpoint() throws Exception {
        when(orgService.currentUserBranches())
                .thenReturn(List.of(branchDto("SCL", "Sucursal Centro", true)));

        mockMvc.perform(get("/v1/org/me/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("SCL"));
    }


    @Test
    void shouldListCurrentUserPrimaryBranchInMeBranchEndpoint() throws Exception {
        when(orgService.currentUserBranch())
                .thenReturn(branchDto("SCL", "Sucursal Centro", true));

        mockMvc.perform(get("/v1/org/me/branch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SCL"));
    }

    @Test
    void shouldReturn401WhenCurrentUserBranchesWithoutAuthentication() throws Exception {
        doThrow(new AuthenticationCredentialsNotFoundException("Usuario no autenticado"))
                .when(orgService).currentUserBranches();

        mockMvc.perform(get("/v1/org/me/branches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldValidateCurrentUserBranch() throws Exception {
        UUID branchId = UUID.randomUUID();

        mockMvc.perform(get("/v1/org/me/branches/{branchId}/validate", branchId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenValidatingBranchWithoutAssignment() throws Exception {
        UUID branchId = UUID.randomUUID();
        doThrow(new AccessDeniedException("Unauthorized"))
                .when(orgService).validateCurrentUserBranch(branchId);

        mockMvc.perform(get("/v1/org/me/branches/{branchId}/validate", branchId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    private BranchDTO branchDto(String code, String name, Boolean active) {
        return new BranchDTO(
                UUID.randomUUID().toString(),
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
