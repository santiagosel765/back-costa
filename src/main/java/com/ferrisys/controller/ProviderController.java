package com.ferrisys.controller;

import com.ferrisys.common.api.ApiResponse;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.ProviderDTO;
import com.ferrisys.service.business.ProviderService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/save")
    public ApiResponse<ProviderDTO> save(@RequestBody ProviderDTO dto) {
        return ApiResponse.single(providerService.saveOrUpdate(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> disable(@PathVariable UUID id) {
        providerService.disable(id);
        return ApiResponse.single(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderDTO> getById(@PathVariable UUID id) {
        return ApiResponse.single(providerService.getById(id));
    }

    @GetMapping("/list")
    public ApiResponse<List<ProviderDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ProviderDTO> response = providerService.list(page, size);
        return ApiResponse.list(response.getData(), response.getTotalItems(), response.getCurrentPage(), response.getPageSize(), response.getTotalPages());
    }
}
