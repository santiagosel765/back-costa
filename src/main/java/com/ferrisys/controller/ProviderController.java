package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;
import com.ferrisys.service.business.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/providers")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "modules.providers", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/save")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('providers')")
    public void save(@RequestBody ProviderDTO dto) {
        providerService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('providers')")
    public void disable(@RequestParam UUID id) {
        providerService.disable(id);
    }

    @GetMapping("/list")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('providers')")
    public PageResponse<ProviderDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return providerService.list(page, size);
    }
}
