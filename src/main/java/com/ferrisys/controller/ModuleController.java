package com.ferrisys.controller;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.service.impl.ModuleServiceImpl;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/modules")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MODULE_CORE_AUTH') or hasRole('ADMIN')")
public class ModuleController {

    private final ModuleServiceImpl moduleService;

    @PostMapping("/save")
    public void saveOrUpdate(@Valid @RequestBody ModuleDTO dto) {
        moduleService.saveOrUpdate(dto);
    }

    @GetMapping("/list")
    public PageResponse<ModuleDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return moduleService.getAll(page, size);
    }

    @PostMapping("/disable")
    public void disable(@RequestParam UUID id) {
        moduleService.disableModule(id);
    }
}
