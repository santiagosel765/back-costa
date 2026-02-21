package com.ferrisys.config.license;

import com.ferrisys.service.FeatureFlagService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ModuleLicenseInterceptor implements HandlerInterceptor {

    private final FeatureFlagService featureFlagService;

    public ModuleLicenseInterceptor(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireModule requirement = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequireModule.class);
        if (requirement == null) {
            requirement = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequireModule.class);
        }

        if (requirement == null) {
            return true;
        }

        featureFlagService.assertModuleEnabled(requirement.value());
        return true;
    }
}
