package com.ferrisys.controller;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.service.impl.ModuleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleServiceImpl moduleService;

    @PostMapping("/save")
    public void saveOrUpdate(@RequestBody ModuleDTO dto) {
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
