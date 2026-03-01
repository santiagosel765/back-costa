package com.ferrisys.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.config.CurrencyDTO;
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
import com.ferrisys.service.config.ConfigCatalogService;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ConfigController.class,
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
class ConfigControllerIntegrationTest {

    @MockBean
    private TenantContextHolder tenantContextHolder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigCatalogService service;

    @MockBean
    private ModuleLicenseService moduleLicenseService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private TenantResolver tenantResolver;

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
    void shouldListCurrencies() throws Exception {
        when(service.listCurrencies(1, 10, ""))
                .thenReturn(new PageResponse<>(List.of(new CurrencyDTO(UUID.randomUUID().toString(), "USD", "Dollar", null, "$", 2, false, null, true, null)), 1, 1, 1, 10));

        mockMvc.perform(get("/v1/config/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.data[0].code").value("USD"));
    }

    @Test
    void shouldReturn404WhenCurrencyNotFound() throws Exception {
        when(service.updateCurrency(any(), any())).thenThrow(new NotFoundException("Moneda no encontrada"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/config/currencies/{id}", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{\"code\":\"USD\",\"name\":\"Dollar\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ENTITY_NOT_FOUND"));
    }
}
