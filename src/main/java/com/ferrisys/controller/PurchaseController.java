package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.PurchaseDTO;
import com.ferrisys.service.business.PurchaseService;
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
@RequestMapping("/v1/purchases")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "modules.purchases", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/save")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('purchases')")
    public void save(@RequestBody PurchaseDTO dto) {
        purchaseService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('purchases')")
    public void disable(@RequestParam UUID id) {
        purchaseService.disable(id);
    }

    @GetMapping("/list")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('purchases')")
    public PageResponse<PurchaseDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return purchaseService.list(page, size);
    }
}
