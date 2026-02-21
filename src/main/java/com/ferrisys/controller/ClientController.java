package com.ferrisys.controller;

import com.ferrisys.common.dto.ClientDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.config.license.RequireModule;
import com.ferrisys.service.business.ClientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@RequireModule("clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/save")
    public void save(@RequestBody ClientDTO dto) {
        clientService.saveOrUpdate(dto);
    }

    @PostMapping("/disable")
    public void disable(@RequestParam UUID id) {
        clientService.disable(id);
    }

    @GetMapping("/list")
    public PageResponse<ClientDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return clientService.list(page, size);
    }
}
