package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;
import com.ferrisys.config.license.RequireModule;
import com.ferrisys.service.business.ProviderService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/providers")
@RequiredArgsConstructor
@RequireModule("providers")
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/save")
    public void save(@RequestBody ProviderDTO dto) {
        providerService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    public void disable(@RequestParam UUID id) {
        providerService.disable(id);
    }

    @GetMapping("/list")
    public PageResponse<ProviderDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return providerService.list(page, size);
    }
}
