package com.ferrisys.controller;

import com.ferrisys.common.dto.AuthResponse;
import com.ferrisys.common.dto.ChangePasswordRequest;
import com.ferrisys.common.dto.LoginRequest;
import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.dto.authcontext.AuthContextResponse;
import com.ferrisys.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    // SaaS B2B policy: user registration is restricted to IAM privileged users.
    @PreAuthorize("hasAuthority('MODULE_CORE_DE_AUTENTICACION') or hasRole('ADMIN')")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request.getUsername(), request.getPassword());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePasswordForCurrentUser(request.currentPassword(), request.newPassword());
    }

    @GetMapping("/modules")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ModuleDTO> getUserModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getModulesForCurrentUser(page, size);
    }

    @GetMapping("/me/context")
    @ResponseStatus(HttpStatus.OK)
    public AuthContextResponse getCurrentContext() {
        return userService.getContextForCurrentUser();
    }
}
