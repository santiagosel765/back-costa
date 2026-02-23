package com.ferrisys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.exception.ModuleNotLicensedException;
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
import com.ferrisys.service.business.ClientService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, ModuleGuardWebConfig.class, ModuleLicenseInterceptor.class, ModuleResolver.class, TenantContextHolder.class})
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ModuleLicenseService moduleLicenseService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId.toString());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldReturn403WhenTenantHasNoModuleLicense() throws Exception {
        doThrow(new ModuleNotLicensedException("Module not licensed"))
                .when(moduleLicenseService)
                .assertLicensed(eq(tenantId), eq("clients"));

        mockMvc.perform(get("/v1/clients/list"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("MODULE_NOT_LICENSED"))
                .andExpect(jsonPath("$.message").value("Module not licensed"));
    }

    @Test
    void shouldReturn200WhenLicenseIsActive() throws Exception {
        when(clientService.list(0, 10))
                .thenReturn(new PageResponse<>(List.of(ClientDTO.builder().id(UUID.randomUUID()).name("ACME").build()), 1, 1, 0, 10));

        mockMvc.perform(get("/v1/clients/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("ACME"))
                .andExpect(jsonPath("$.total").value(1));

        verify(moduleLicenseService).assertLicensed(eq(tenantId), eq("clients"));
    }

    @Test
    void shouldReturn404ForCrossTenantLookup() throws Exception {
        UUID clientId = UUID.randomUUID();
        when(clientService.getById(any())).thenThrow(new NotFoundException("Cliente no encontrado"));

        mockMvc.perform(get("/v1/clients/{id}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ENTITY_NOT_FOUND"));
    }
}
