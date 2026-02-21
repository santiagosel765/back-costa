package com.ferrisys.controller;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.service.business.ClientService;
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
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "modules.clients", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/save")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('clients')")
    public void save(@RequestBody ClientDTO dto) {
        clientService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('clients')")
    public void disable(@RequestParam UUID id) {
        clientService.disable(id);
    }

    @GetMapping("/list")
    @PreAuthorize("@featureFlagService.enabledForCurrentUser('clients')")
    public PageResponse<ClientDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientService.list(page, size);
    }
}
