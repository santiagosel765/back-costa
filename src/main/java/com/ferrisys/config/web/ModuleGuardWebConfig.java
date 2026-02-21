package com.ferrisys.config.web;

import com.ferrisys.config.license.ModuleLicenseInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ModuleGuardWebConfig implements WebMvcConfigurer {

    private final ModuleLicenseInterceptor moduleLicenseInterceptor;

    public ModuleGuardWebConfig(ModuleLicenseInterceptor moduleLicenseInterceptor) {
        this.moduleLicenseInterceptor = moduleLicenseInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduleLicenseInterceptor).addPathPatterns("/v1/**");
    }
}
