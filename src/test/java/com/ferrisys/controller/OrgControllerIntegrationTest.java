package com.ferrisys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.org.BranchDTO;
import com.ferrisys.common.dto.org.UserBranchAssignmentDTO;
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
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrgController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ModuleGuardWebConfig.class, ModuleLicenseInterceptor.class, ModuleResolver.class, TenantContextHolder.class})
@ImportAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
class OrgControllerIntegrationTest {

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
        when(orgService.listBranches(0, 10, ""))
                .thenReturn(new PageResponse<>(List.of(new BranchDTO(UUID.randomUUID().toString(), "MTR", "Matriz", null, null, true, null)), 1, 1, 0, 10));

        mockMvc.perform(get("/v1/org/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Matriz"));
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
        when(orgService.listUserBranchAssignments(eq(userId), eq(null), eq(0), eq(10)))
                .thenReturn(new PageResponse<>(List.of(new UserBranchAssignmentDTO(UUID.randomUUID().toString(), userId.toString(), UUID.randomUUID().toString(), true, null)), 1, 1, 0, 10));

        mockMvc.perform(get("/v1/org/user-branch-assignments").param("userId", userId.toString()))
                .andExpect(status().isOk())
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
    void shouldReturn400WhenListingAssignmentsWithoutFilters() throws Exception {
        mockMvc.perform(get("/v1/org/user-branch-assignments"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
