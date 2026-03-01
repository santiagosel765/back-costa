package com.ferrisys.controller;

import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.dto.auth.RoleModuleMetadataDto;
import com.ferrisys.common.dto.auth.RoleModulesDto;
import com.ferrisys.common.dto.auth.RolePermissionsDto;
import com.ferrisys.common.entity.license.ModuleLicense;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthRoleModule;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.entity.user.UserStatus;
import com.ferrisys.common.enums.DefaultUserStatus;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.ConflictException;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.common.util.ModuleKeyNormalizer;
import com.ferrisys.core.tenant.TenantContextHolder;
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
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
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
        "@featureFlagService.enabledForCurrentUser('core-auth') and (hasAuthority('MODULE_CORE_AUTH') or hasRole('ADMIN'))")
public class AuthAdminController {

    private static final Map<String, String> MODULE_KEY_ALIASES = Map.ofEntries(
            Map.entry("CONFIGURACION", "CONFIG"),
            Map.entry("SETTINGS", "CONFIG"),
            Map.entry("ORGANIZACION", "ORG"),
            Map.entry("SUCURSALES_Y_ORGANIZACIONES", "ORG"),
            Map.entry("ORG_BRANCH", "ORG"),
            Map.entry("INVENTARIO", "INVENTORY"),
            Map.entry("COMPRAS", "PURCHASE"),
            Map.entry("VENTAS", "SALES"),
            Map.entry("CORE_DE_AUTENTICACION", "CORE_AUTH"));

    private static final Map<String, String> MODULE_CANONICAL_NAMES = Map.ofEntries(
            Map.entry("CONFIG", "Configuración"),
            Map.entry("ORG", "Organización"),
            Map.entry("INVENTORY", "Inventario"),
            Map.entry("PURCHASE", "Compras"),
            Map.entry("SALES", "Ventas"),
            Map.entry("CORE_AUTH", "Core de Autenticación"));

    private static final Map<String, String> MODULE_BASE_ROUTES = Map.ofEntries(
            Map.entry("CONFIG", "/main/config"),
            Map.entry("ORG", "/main/org"),
            Map.entry("INVENTORY", "/main/inventory"),
            Map.entry("PURCHASE", "/main/purchase"),
            Map.entry("SALES", "/main/sales"),
            Map.entry("CORE_AUTH", "/main/auth"));

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleRepository roleModuleRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final ModuleLicenseRepository moduleLicenseRepository;
    private final UserService userService;
    private final AuditEventService auditEventService;
    private final TenantContextHolder tenantContextHolder;

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
                .tenantId(tenantContextHolder.requireTenantId())
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
        UUID tenantId = tenantContextHolder.requireTenantId();
        return moduleRepository.findByTenantIdAndStatusOrderByNameAsc(tenantId, 1, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    @GetMapping("/modules/{id}")
    public AuthModule getModule(@PathVariable UUID id) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        return moduleRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new NotFoundException("Module not found"));
    }

    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthModule createModule(@Valid @RequestBody AdminModuleRequest request) {
        AuthModule module = AuthModule.builder()
                .moduleKey(ModuleKeyNormalizer.normalize(request.name()))
                .name(request.name())
                .description(request.description())
                .status(request.status())
                .tenantId(tenantContextHolder.requireTenantId())
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
        UUID tenantId = tenantContextHolder.requireTenantId();
        AuthModule module = moduleRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new NotFoundException("Module not found"));
        module.setModuleKey(ModuleKeyNormalizer.normalize(request.name()));
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
        UUID tenantId = tenantContextHolder.requireTenantId();
        AuthModule module = moduleRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new NotFoundException("Module not found"));
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
        UUID tenantId = tenantContextHolder.requireTenantId();
        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new NotFoundException("Role not found for tenant"));
        List<AuthRoleModule> assignments = roleModuleRepository.findByRoleIdAndTenantIdAndStatus(roleId, tenantId, 1);
        return buildRoleModulesDto(role, assignments);
    }

    @GetMapping("/role-permissions")
    @Transactional
    public RolePermissionsDto getRolePermissions(@RequestParam("roleId") UUID roleId) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new NotFoundException("Role not found for tenant"));
        List<AuthRoleModule> assignments = roleModuleRepository.findByRoleIdAndTenantIdAndStatus(roleId, tenantId, 1);
        return buildRolePermissionsDto(role, assignments);
    }

    @PostMapping("/role-modules")
    @Transactional
    public void saveRoleModules(@Valid @RequestBody RoleModuleRequest request) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        Role role = roleRepository.findByIdAndTenantId(request.roleId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Role not found for tenant"));
        updateRoleModulesIdempotent(role, request.moduleIds(), tenantId);
    }

    @PutMapping("/role-modules/{roleId}")
    @Transactional
    public RoleModulesDto updateRoleModules(@PathVariable UUID roleId, @Valid @RequestBody RoleModulesDto request) {
        UUID tenantId = tenantContextHolder.requireTenantId();
        String currentUser = getCurrentUsername();

        if (request.getModuleIds() == null || request.getModuleIds().isEmpty()) {
            throw new BadRequestException("moduleIds cannot be empty");
        }

        try {
            Role role = roleRepository
                    .findByIdAndTenantId(roleId, tenantId)
                    .orElseThrow(() -> new NotFoundException("Role not found for tenant"));
            updateRoleModulesIdempotent(role, request.getModuleIds(), tenantId);
            List<AuthRoleModule> assignments = roleModuleRepository.findByRoleIdAndTenantIdAndStatus(roleId, tenantId, 1);
            return buildRoleModulesDto(role, assignments);
        } catch (DataIntegrityViolationException | ConstraintViolationException exception) {
            log.error(
                    "Data integrity error updating role modules. roleId={}, tenantId={}, currentUser={}, moduleIds={}",
                    roleId,
                    tenantId,
                    currentUser,
                    request.getModuleIds(),
                    exception);
            throw new ConflictException("Unable to update role modules due to data integrity constraints");
        } catch (RuntimeException exception) {
            log.error(
                    "Unexpected error updating role modules. roleId={}, tenantId={}, currentUser={}, moduleIds={}",
                    roleId,
                    tenantId,
                    currentUser,
                    request.getModuleIds(),
                    exception);
            throw exception;
        }
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

    private void updateRoleModulesIdempotent(Role role, List<UUID> requestedModuleIds, UUID tenantId) {
        Set<UUID> uniqueModuleIds = new HashSet<>(requestedModuleIds);
        List<AuthModule> tenantModules = moduleRepository.findAllById(uniqueModuleIds).stream()
                .filter(module -> tenantId.equals(module.getTenantId()))
                .filter(module -> module.getStatus() != null && module.getStatus() == 1)
                .toList();

        Map<UUID, AuthModule> validModulesById = tenantModules.stream().collect(Collectors.toMap(AuthModule::getId, module -> module));
        Set<UUID> validModuleIds = validModulesById.keySet();
        if (validModuleIds.size() != uniqueModuleIds.size()) {
            Set<UUID> invalidModuleIds = new HashSet<>(uniqueModuleIds);
            invalidModuleIds.removeAll(validModuleIds);
            throw new BadRequestException("Invalid moduleIds for tenant: " + invalidModuleIds);
        }

        List<AuthRoleModule> existingAssignments = roleModuleRepository.findByRoleIdAndTenantId(role.getId(), tenantId);
        Map<UUID, AuthRoleModule> assignmentsByModule = new HashMap<>();
        Set<UUID> currentActiveModuleIds = new HashSet<>();

        for (AuthRoleModule assignment : existingAssignments) {
            UUID moduleId = assignment.getModule().getId();
            assignmentsByModule.put(moduleId, assignment);
            if (assignment.getStatus() != null && assignment.getStatus() == 1) {
                currentActiveModuleIds.add(moduleId);
            }
        }

        Set<UUID> toAdd = new HashSet<>(validModuleIds);
        toAdd.removeAll(currentActiveModuleIds);

        Set<UUID> toRemove = new HashSet<>(currentActiveModuleIds);
        toRemove.removeAll(validModuleIds);

        for (UUID moduleId : toAdd) {
            AuthRoleModule existingAssignment = assignmentsByModule.get(moduleId);
            if (existingAssignment != null) {
                existingAssignment.setStatus(1);
                existingAssignment.setTenantId(tenantId);
                roleModuleRepository.save(existingAssignment);
                continue;
            }

            AuthModule module = validModulesById.get(moduleId);
            if (module == null) {
                throw new ConflictException("Module not found for tenant");
            }
            AuthRoleModule assignment = AuthRoleModule.builder()
                    .role(role)
                    .module(module)
                    .status(1)
                    .tenantId(tenantId)
                    .build();
            roleModuleRepository.save(assignment);
        }

        for (UUID moduleId : toRemove) {
            AuthRoleModule existingAssignment = assignmentsByModule.get(moduleId);
            if (existingAssignment != null) {
                existingAssignment.setStatus(0);
                existingAssignment.setTenantId(tenantId);
                roleModuleRepository.save(existingAssignment);
            }
        }
    }

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "anonymous";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private RoleModulesDto buildRoleModulesDto(Role role, List<AuthRoleModule> assignments) {
        List<AuthModule> activeModules = assignments.stream()
                .filter(assignment -> assignment.getStatus() == null || assignment.getStatus() == 1)
                .map(AuthRoleModule::getModule)
                .toList();

        Map<String, AuthModule> canonicalModulesByKey = activeModules.stream()
                .collect(Collectors.toMap(
                        module -> resolveModuleKey(module),
                        module -> module,
                        this::pickCanonicalModule,
                        LinkedHashMap::new));

        List<AuthModule> normalizedModules = canonicalModulesByKey.values().stream()
                .sorted(Comparator.comparing(
                        module -> resolveModuleKey(module),
                        Comparator.nullsLast(String::compareTo)))
                .toList();

        List<UUID> moduleIds = normalizedModules.stream().map(AuthModule::getId).toList();
        List<RoleModuleMetadataDto> moduleMetadata = normalizedModules.stream()
                .map(module -> RoleModuleMetadataDto.builder()
                        .id(module.getId())
                        .key(resolveModuleKey(module))
                        .name(resolveModuleName(module))
                        .baseRoute(resolveModuleBaseRoute(module))
                        .build())
                .toList();

        return RoleModulesDto.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .moduleIds(moduleIds)
                .modules(moduleMetadata)
                .build();
    }

    private RolePermissionsDto buildRolePermissionsDto(Role role, List<AuthRoleModule> assignments) {
        Map<String, RolePermissionsDto.ActionPermissionsDto> permissions = assignments.stream()
                .filter(assignment -> assignment.getStatus() == null || assignment.getStatus() == 1)
                .map(AuthRoleModule::getModule)
                .collect(Collectors.toMap(
                        module -> resolveModuleKey(module),
                        module -> RolePermissionsDto.ActionPermissionsDto.builder()
                                .read(true)
                                .write(true)
                                .delete(true)
                                .build(),
                        (existing, replacement) -> existing));

        return RolePermissionsDto.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .permissions(permissions)
                .build();
    }

    private String resolveModuleKey(AuthModule module) {
        String normalized = module != null && module.getModuleKey() != null && !module.getModuleKey().isBlank()
                ? ModuleKeyNormalizer.normalize(module.getModuleKey())
                : ModuleKeyNormalizer.normalize(module != null ? module.getName() : null);
        if (normalized == null) {
            return null;
        }

        return MODULE_KEY_ALIASES.getOrDefault(normalized, normalized);
    }

    private AuthModule pickCanonicalModule(AuthModule first, AuthModule second) {
        return isCanonicalModule(first) ? first : second;
    }

    private boolean isCanonicalModule(AuthModule module) {
        String key = resolveModuleKey(module);
        return key != null && key.equals(ModuleKeyNormalizer.normalize(module.getModuleKey()));
    }

    private String resolveModuleName(AuthModule module) {
        String key = resolveModuleKey(module);
        return MODULE_CANONICAL_NAMES.getOrDefault(key, module.getName());
    }

    private String resolveModuleBaseRoute(AuthModule module) {
        String key = resolveModuleKey(module);
        if (key == null || key.isBlank()) {
            return null;
        }
        return MODULE_BASE_ROUTES.getOrDefault(key, "/main/" + key.toLowerCase());
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
