package com.ferrisys.config.bootstrap;

import com.ferrisys.common.entity.config.Parameter;
import com.ferrisys.repository.ParameterRepository;
import com.ferrisys.repository.TenantRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConfigParameterBootstrap implements CommandLineRunner {

    private static final Map<String, String> DEFAULT_PARAMETERS = Map.of(
            "sales.quote.requires_approval", "false",
            "sales.quote.required_role", "SUPERVISOR",
            "doc.numbering.scope", "BRANCH",
            "storage.mode", "LOCAL",
            "inventory.expiry.enabled", "false");

    private final TenantRepository tenantRepository;
    private final ParameterRepository parameterRepository;

    @Override
    @Transactional
    public void run(String... args) {
        tenantRepository.findAll().forEach(tenant -> DEFAULT_PARAMETERS.forEach((code, value) -> {
            parameterRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenant.getId(), code)
                    .orElseGet(() -> parameterRepository.save(buildDefault(tenant.getId(), code, value)));
        }));
    }

    private Parameter buildDefault(java.util.UUID tenantId, String code, String value) {
        Parameter parameter = new Parameter();
        parameter.setTenantId(tenantId);
        parameter.setCode(code);
        parameter.setName(code);
        parameter.setDescription("Default parameter");
        parameter.setValue(value);
        parameter.setActive(Boolean.TRUE);
        return parameter;
    }
}
