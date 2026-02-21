package com.ferrisys.controller;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.service.UserService;
import com.ferrisys.common.dto.AuthResponse;
import com.ferrisys.common.dto.LoginRequest;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.authenticate(request.getUsername(), request.getPassword());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse recoverPassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @RequestParam String userToken) {
        return userService.recoverPassword(newPassword, confirmPassword, userToken);
    }

    @GetMapping("/modules")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ModuleDTO> getUserModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getModulesForCurrentUser(page, size);
    }
}
