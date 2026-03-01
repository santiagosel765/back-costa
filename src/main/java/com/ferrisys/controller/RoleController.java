package com.ferrisys.controller;

import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RoleDTO;
import com.ferrisys.service.impl.RoleServiceImpl;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MODULE_CORE_AUTH') or hasRole('ADMIN')")
public class RoleController {

    private final RoleServiceImpl roleService;

    @PostMapping("/save")
    public ResponseEntity<String> saveRole(@Valid @RequestBody RoleDTO dto) {
        roleService.saveOrUpdate(dto);
        return ResponseEntity.ok("Rol procesado correctamente");
    }

    @PostMapping("/list")
    public PageResponse<RoleDTO> listRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return roleService.getAll(page, size);
    }

    @PostMapping("/disable")
    public ResponseEntity<String> disableRole(@RequestParam UUID roleId) {
        roleService.disableRole(roleId);
        return ResponseEntity.ok("Rol deshabilitado");
    }
}
