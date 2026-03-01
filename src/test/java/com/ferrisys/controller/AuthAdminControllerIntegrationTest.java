package com.ferrisys.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthRoleModule;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.config.license.ModuleLicenseInterceptor;
import com.ferrisys.config.license.ModuleLicenseService;
import com.ferrisys.config.license.ModuleResolver;
import com.ferrisys.config.security.CustomUserDetailsService;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.config.web.GlobalExceptionHandler;
import com.ferrisys.config.web.ModuleGuardWebConfig;
import com.ferrisys.core.tenant.TenantContextHolder;
import com.ferrisys.core.tenant.TenantResolver;
import com.ferrisys.repository.AuthUserRoleRepository;
import com.ferrisys.repository.ModuleLicenseRepository;
import com.ferrisys.repository.ModuleRepository;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.repository.RoleRepository;
import com.ferrisys.repository.UserRepository;
import com.ferrisys.service.UserService;
import com.ferrisys.service.audit.AuditEventService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AuthAdminController.class,
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
class AuthAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private ModuleRepository moduleRepository;

    @MockBean
    private RoleModuleRepository roleModuleRepository;

    @MockBean
    private AuthUserRoleRepository authUserRoleRepository;

    @MockBean
    private ModuleLicenseRepository moduleLicenseRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private TenantContextHolder tenantContextHolder;

    @MockBean
    private ModuleLicenseService moduleLicenseService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private TenantResolver tenantResolver;

    @Test
    void shouldReturnRolePermissionsForAdminWithInventoryWriteEnabled() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        Role role = Role.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Admin role")
                .status(1)
                .tenantId(tenantId)
                .build();

        AuthModule inventoryModule = AuthModule.builder()
                .id(UUID.randomUUID())
                .name("Inventario")
                .description("Inventario")
                .status(1)
                .tenantId(tenantId)
                .build();

        AuthModule authCoreModule = AuthModule.builder()
                .id(UUID.randomUUID())
                .name("Core de Autenticación")
                .description("Auth")
                .status(1)
                .tenantId(tenantId)
                .build();

        when(tenantContextHolder.requireTenantId()).thenReturn(tenantId);
        when(roleRepository.findByIdAndTenantId(roleId, tenantId)).thenReturn(Optional.of(role));
        when(roleModuleRepository.findByRoleIdAndTenantIdAndStatus(roleId, tenantId, 1)).thenReturn(List.of(
                AuthRoleModule.builder().role(role).module(inventoryModule).status(1).tenantId(tenantId).build(),
                AuthRoleModule.builder().role(role).module(authCoreModule).status(1).tenantId(tenantId).build()
        ));

        mockMvc.perform(get("/v1/auth/admin/role-permissions").param("roleId", roleId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.permissions.INVENTORY.read").value(true))
                .andExpect(jsonPath("$.permissions.INVENTORY.write").value(true))
                .andExpect(jsonPath("$.permissions.AUTH_CORE.read").value(true));
    }
}
