package com.ferrisys.controller;

import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.dto.auth.RoleModulesDto;
import com.ferrisys.common.entity.license.ModuleLicense;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthRoleModule;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.entity.user.UserStatus;
import com.ferrisys.common.enums.DefaultUserStatus;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.repository.AuthUserRoleRepository;
import com.ferrisys.repository.ModuleLicenseRepository;
import com.ferrisys.repository.ModuleRepository;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.repository.RoleRepository;
import com.ferrisys.repository.UserRepository;
import com.ferrisys.service.UserService;
import com.ferrisys.service.audit.AuditEventPublishRequest;
import com.ferrisys.service.audit.AuditEventService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "@featureFlagService.enabledForCurrentUser('core-de-autenticacion') and (hasAuthority('MODULE_CORE_DE_AUTENTICACION') or hasRole('ADMIN'))")
public class AuthAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleRepository roleModuleRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final ModuleLicenseRepository moduleLicenseRepository;
    private final UserService userService;
    private final AuditEventService auditEventService;

    @GetMapping("/users")
    public List<AdminUserResponse> listUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, List<AuthUserRole>> assignments = new HashMap<>();
        authUserRoleRepository.findAllByUserIdIn(users.stream().map(User::getId).toList())
                .forEach(assignment -> assignments.computeIfAbsent(assignment.getUser().getId(), key -> new ArrayList<>()).add(assignment));

        return users.stream()
                .map(user -> mapUser(user, assignments.getOrDefault(user.getId(), Collections.emptyList())))
                .toList();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUser(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        List<AuthUserRole> assignments = authUserRoleRepository.findAllByUserIdIn(List.of(user.getId()));
        return mapUser(user, assignments);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody AdminUserRequest request) {
        RegisterRequest register = new RegisterRequest();
        register.setUsername(request.username());
        register.setEmail(request.email());
        register.setFullName(request.fullName());
        register.setPassword(request.password());
        userService.registerUser(register);
        User user = userRepository.findByUsername(request.username()).orElseThrow(() -> new NotFoundException("User not created"));
        if (request.status() != null) {
            user.setStatus(UserStatus.fromCode(request.status()));
            user = userRepository.save(user);
        }
        syncUserRoles(user, request.roleIds());
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .action("USER_CREATED")
                .entityType("USER")
                .entityId(user.getId().toString())
                .payload(Map.of("username", user.getUsername(), "email", user.getEmail()))
                .build());
        return mapUser(user, authUserRoleRepository.findAllByUserIdIn(List.of(user.getId())));
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody AdminUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setStatus(UserStatus.fromCode(request.status()));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(new BCryptPasswordEncoder().encode(request.password()));
        }
        User saved = userRepository.save(user);
        syncUserRoles(saved, request.roleIds());
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenant() != null ? saved.getTenant().getId() : null)
                .action("USER_UPDATED")
                .entityType("USER")
                .entityId(saved.getId().toString())
                .payload(Map.of("username", saved.getUsername(), "email", saved.getEmail()))
                .build());
        return mapUser(saved, authUserRoleRepository.findAllByUserIdIn(List.of(saved.getId())));
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.deleteById(id);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .action("USER_DELETED")
                .entityType("USER")
                .entityId(id.toString())
                .payload(Map.of("username", user.getUsername()))
                .build());
    }

    @PostMapping("/user-roles")
    @Transactional
    public void assignUserRole(@Valid @RequestBody UserRoleRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new NotFoundException("User not found"));
        roleRepository.findById(request.roleId()).orElseThrow(() -> new NotFoundException("Role not found"));
        syncUserRoles(user, List.of(request.roleId()));
    }

    @GetMapping("/roles")
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/roles/{id}")
    public Role getRole(@PathVariable UUID id) {
        return roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found"));
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public Role createRole(@Valid @RequestBody AdminRoleRequest request) {
        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .status(request.status())
                .build();
        Role saved = roleRepository.save(role);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenantId())
                .action("ROLE_CREATED")
                .entityType("ROLE")
                .entityId(saved.getId().toString())
                .payload(Map.of("name", saved.getName()))
                .build());
        return saved;
    }

    @PutMapping("/roles/{id}")
    public Role updateRole(@PathVariable UUID id, @Valid @RequestBody AdminRoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found"));
        role.setName(request.name());
        role.setDescription(request.description());
        role.setStatus(request.status());
        Role saved = roleRepository.save(role);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenantId())
                .action("ROLE_UPDATED")
                .entityType("ROLE")
                .entityId(saved.getId().toString())
                .payload(Map.of("name", saved.getName()))
                .build());
        return saved;
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable UUID id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found"));
        roleRepository.deleteById(id);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(role.getTenantId())
                .action("ROLE_DELETED")
                .entityType("ROLE")
                .entityId(id.toString())
                .payload(Map.of("name", role.getName()))
                .build());
    }

    @GetMapping("/modules")
    public List<AuthModule> listModules() {
        return moduleRepository.findAll();
    }

    @GetMapping("/modules/{id}")
    public AuthModule getModule(@PathVariable UUID id) {
        return moduleRepository.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
    }

    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthModule createModule(@Valid @RequestBody AdminModuleRequest request) {
        AuthModule module = AuthModule.builder()
                .name(request.name())
                .description(request.description())
                .status(request.status())
                .build();
        AuthModule saved = moduleRepository.save(module);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenantId())
                .action("MODULE_CREATED")
                .entityType("MODULE")
                .entityId(saved.getId().toString())
                .payload(Map.of("name", saved.getName()))
                .build());
        return saved;
    }

    @PutMapping("/modules/{id}")
    public AuthModule updateModule(@PathVariable UUID id, @Valid @RequestBody AdminModuleRequest request) {
        AuthModule module = moduleRepository.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
        module.setName(request.name());
        module.setDescription(request.description());
        module.setStatus(request.status());
        AuthModule saved = moduleRepository.save(module);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenantId())
                .action("MODULE_UPDATED")
                .entityType("MODULE")
                .entityId(saved.getId().toString())
                .payload(Map.of("name", saved.getName()))
                .build());
        return saved;
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable UUID id) {
        AuthModule module = moduleRepository.findById(id).orElseThrow(() -> new NotFoundException("Module not found"));
        moduleRepository.deleteById(id);
        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(module.getTenantId())
                .action("MODULE_DELETED")
                .entityType("MODULE")
                .entityId(id.toString())
                .payload(Map.of("name", module.getName()))
                .build());
    }

    @GetMapping("/role-modules")
    @Transactional
    public RoleModulesDto getRoleModules(@RequestParam("roleId") UUID roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
        List<AuthRoleModule> assignments = roleModuleRepository.findByRoleIdAndStatus(roleId, 1);
        return buildRoleModulesDto(role, assignments);
    }

    @PostMapping("/role-modules")
    @Transactional
    public void saveRoleModules(@Valid @RequestBody RoleModuleRequest request) {
        Role role = roleRepository.findById(request.roleId()).orElseThrow(() -> new NotFoundException("Role not found"));
        persistRoleModules(role, request.moduleIds());
    }

    @PutMapping("/role-modules/{roleId}")
    @Transactional
    public RoleModulesDto updateRoleModules(@PathVariable UUID roleId, @Valid @RequestBody RoleModulesDto request) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
        persistRoleModules(role, request.getModuleIds());
        List<AuthRoleModule> assignments = roleModuleRepository.findByRoleIdAndStatus(roleId, 1);
        return buildRoleModulesDto(role, assignments);
    }

    @GetMapping("/module-licenses")
    public List<ModuleLicense> listLicenses() {
        return moduleLicenseRepository.findAll();
    }

    @PostMapping("/module-licenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleLicense createLicense(@Valid @RequestBody ModuleLicenseRequest request) {
        AuthModule module = moduleRepository.findById(request.moduleId()).orElseThrow(() -> new NotFoundException("Module not found"));
        ModuleLicense license = ModuleLicense.builder()
                .tenantId(request.tenantId())
                .module(module)
                .enabled(request.enabled())
                .expiresAt(request.expiresAt())
                .build();
        return moduleLicenseRepository.save(license);
    }

    private void syncUserRoles(User user, List<UUID> roleIds) {
        authUserRoleRepository.deleteByUserId(user.getId());
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        for (UUID roleId : roleIds) {
            Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Role not found"));
            AuthUserRole assignment = AuthUserRole.builder()
                    .user(user)
                    .role(role)
                    .status(1)
                    .build();
            authUserRoleRepository.save(assignment);
        }
    }

    private void persistRoleModules(Role role, List<UUID> moduleIds) {
        roleModuleRepository.deleteByRole(role);
        if (moduleIds == null || moduleIds.isEmpty()) {
            return;
        }

        for (UUID moduleId : moduleIds) {
            AuthModule module = moduleRepository.findById(moduleId).orElseThrow(() -> new NotFoundException("Module not found"));
            AuthRoleModule assignment = AuthRoleModule.builder()
                    .role(role)
                    .module(module)
                    .status(1)
                    .build();
            roleModuleRepository.save(assignment);
        }
    }

    private RoleModulesDto buildRoleModulesDto(Role role, List<AuthRoleModule> assignments) {
        List<UUID> moduleIds = assignments.stream()
                .filter(assignment -> assignment.getStatus() == null || assignment.getStatus() == 1)
                .map(assignment -> assignment.getModule().getId())
                .toList();

        return RoleModulesDto.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .moduleIds(moduleIds)
                .build();
    }

    private AdminUserResponse mapUser(User user, List<AuthUserRole> assignments) {
        List<AuthUserRole> activeAssignments = assignments.stream()
                .filter(assignment -> assignment.getStatus() == null || assignment.getStatus() == 1)
                .toList();

        List<UUID> roleIds = activeAssignments.stream().map(a -> a.getRole().getId()).toList();
        List<String> roleNames = activeAssignments.stream().map(a -> a.getRole().getName()).toList();
        int statusCode = user.getStatus() != null && DefaultUserStatus.ACTIVE.getId().equals(user.getStatus().getStatusId()) ? 1 : 0;

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                statusCode,
                roleIds,
                roleNames);
    }

    public record AdminUserRequest(
            @NotBlank(message = "username is required") String username,
            @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
            @NotBlank(message = "fullName is required") String fullName,
            @NotBlank(message = "password is required") String password,
            Integer status,
            List<UUID> roleIds) {}
    public record AdminUserResponse(UUID id, String username, String email, String fullName, Integer status, List<UUID> roleIds, List<String> roleNames) {}
    public record AdminRoleRequest(@NotBlank(message = "name is required") String name, @NotBlank(message = "description is required") String description, @NotNull(message = "status is required") Integer status) {}
    public record AdminModuleRequest(@NotBlank(message = "name is required") String name, @NotBlank(message = "description is required") String description, @NotNull(message = "status is required") Integer status) {}
    public record RoleModuleRequest(@NotNull(message = "roleId is required") UUID roleId, @NotEmpty(message = "moduleIds is required") List<UUID> moduleIds) {}
    public record UserRoleRequest(@NotNull(message = "userId is required") UUID userId, @NotNull(message = "roleId is required") UUID roleId) {}
    public record ModuleLicenseRequest(@NotNull(message = "tenantId is required") UUID tenantId, @NotNull(message = "moduleId is required") UUID moduleId, @NotNull(message = "enabled is required") Boolean enabled, OffsetDateTime expiresAt) {}
}
