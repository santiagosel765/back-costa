package com.ferrisys.service.impl;

import com.ferrisys.common.dto.AuthResponse;
import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.dto.authcontext.AuthContextModuleDto;
import com.ferrisys.common.dto.authcontext.AuthContextResponse;
import com.ferrisys.common.dto.authcontext.AuthContextTenantDto;
import com.ferrisys.common.dto.authcontext.AuthContextTokenDto;
import com.ferrisys.common.dto.authcontext.AuthContextUserDto;
import com.ferrisys.common.entity.license.ModuleLicense;
import com.ferrisys.common.entity.tenant.Tenant;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.entity.user.UserStatus;
import com.ferrisys.common.enums.DefaultRole;
import com.ferrisys.common.enums.DefaultUserStatus;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.NotFoundException;
import com.ferrisys.common.util.ModuleKeyNormalizer;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.core.tenant.TenantContext;
import com.ferrisys.mapper.ModuleMapper;
import com.ferrisys.repository.AuthUserRoleRepository;
import com.ferrisys.repository.ModuleLicenseRepository;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.repository.RoleRepository;
import com.ferrisys.repository.TenantRepository;
import com.ferrisys.repository.UserRepository;
import com.ferrisys.repository.UserStatusRepository;
import com.ferrisys.service.FeatureFlagService;
import com.ferrisys.service.UserService;
import com.ferrisys.service.audit.AuditEventPublishRequest;
import com.ferrisys.service.audit.AuditEventService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final RoleModuleRepository roleModuleRepository;
    private final ModuleLicenseRepository moduleLicenseRepository;
    private final JWTUtil jwtUtil;
    private final FeatureFlagService featureFlagService;
    private final ModuleMapper moduleMapper;
    private final TenantRepository tenantRepository;
    private final AuditEventService auditEventService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User getAuthUser(String username) {
        return userRepository.findByUsernameAndStatus(username, new UserStatus(DefaultUserStatus.ACTIVE.getId()))
                .orElseThrow(() -> new NotFoundException("User not found or inactive"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, new UserStatus(DefaultUserStatus.ACTIVE.getId()))
                .orElseThrow(() -> new NotFoundException("User with provided email not found or inactive"));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setStatus(new UserStatus(DefaultUserStatus.ACTIVE.getId()));
        userRepository.save(user);
    }

    @Override
    public AuthUserRole getUserRole(UUID userId) {
        return authUserRoleRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User role not found"));
    }

    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        UserStatus activeStatus = userStatusRepository.findById(DefaultUserStatus.ACTIVE.getId())
                .orElseThrow(() -> new NotFoundException("Estado de usuario no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(activeStatus)
                .tenant(resolveOrCreateTenant(request.getUsername()))
                .build();

        User saved = userRepository.save(user);

        Role defaultRole = roleRepository.findById(DefaultRole.USER.getId())
                .orElseThrow(() -> new NotFoundException("Rol por defecto no encontrado"));

        AuthUserRole userRole = AuthUserRole.builder()
                .user(saved)
                .role(defaultRole)
                .status(1)
                .build();

        authUserRoleRepository.save(userRole);

        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenant() != null ? saved.getTenant().getId() : null)
                .actor(saved.getUsername())
                .actorUserId(saved.getId())
                .action("USER_CREATED")
                .entityType("USER")
                .entityId(saved.getId().toString())
                .payload(java.util.Map.of("username", saved.getUsername(), "email", saved.getEmail()))
                .build());

        AuthArtifacts authArtifacts = buildAuthArtifacts(saved);

        return AuthResponse.builder()
                .token(authArtifacts.token())
                .expiresAt(authArtifacts.expiresAt())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(defaultRole.getName())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse authenticate(String username, String password) {
        User user;
        try {
            user = getAuthUser(username);
        } catch (RuntimeException ex) {
            auditEventService.publish(AuditEventPublishRequest.builder()
                    .actor(username)
                    .action("LOGIN_FAILED")
                    .entityType("AUTH")
                    .entityId(username)
                    .payload(java.util.Map.of("username", username))
                    .build());
            throw ex;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            auditEventService.publish(AuditEventPublishRequest.builder()
                    .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                    .actor(username)
                    .actorUserId(user.getId())
                    .action("LOGIN_FAILED")
                    .entityType("AUTH")
                    .entityId(user.getId().toString())
                    .payload(java.util.Map.of("username", username))
                    .build());
            throw new BadRequestException("Invalid credentials");
        }

        if (user.getTenant() == null) {
            user.setTenant(resolveOrCreateTenant(username));
            user = userRepository.save(user);
        }

        AuthUserRole role = getUserRole(user.getId());
        AuthArtifacts authArtifacts = buildAuthArtifacts(user);

        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
                .actor(username)
                .actorUserId(user.getId())
                .action("LOGIN_SUCCESS")
                .entityType("AUTH")
                .entityId(user.getId().toString())
                .payload(java.util.Map.of("username", username))
                .build());

        return AuthResponse.builder()
                .token(authArtifacts.token())
                .expiresAt(authArtifacts.expiresAt())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role.getRole().getName())
                .build();
    }

    @Override
    public AuthResponse changePasswordForCurrentUser(String currentPassword, String newPassword) {
        String currentUsername = jwtUtil.getCurrentUser();
        User user = getAuthUser(currentUsername);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is invalid");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        AuthUserRole role = getUserRole(saved.getId());

        auditEventService.publish(AuditEventPublishRequest.builder()
                .tenantId(saved.getTenant() != null ? saved.getTenant().getId() : null)
                .actor(saved.getUsername())
                .actorUserId(saved.getId())
                .action("PASSWORD_CHANGED")
                .entityType("USER")
                .entityId(saved.getId().toString())
                .payload(java.util.Map.of("username", saved.getUsername()))
                .build());

        AuthArtifacts authArtifacts = buildAuthArtifacts(saved);

        return AuthResponse.builder()
                .token(authArtifacts.token())
                .expiresAt(authArtifacts.expiresAt())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(role.getRole().getName())
                .build();
    }

    @Override
    public PageResponse<ModuleDTO> getModulesForCurrentUser(int page, int size) {
        String username = jwtUtil.getCurrentUser();
        User user = getAuthUser(username);
        AuthUserRole role = getUserRole(user.getId());

        Page<AuthModule> result = roleModuleRepository.findModulesByRoleId(
                role.getRole().getId(), PageRequest.of(page, size));
        List<AuthModule> filteredModules = result.getContent().stream()
                .filter(module -> featureFlagService.isModuleEnabled(user.getTenant().getId(), module.getName()))
                .toList();
        Page<ModuleDTO> pageDto = new PageImpl<>(
                moduleMapper.toDtoList(filteredModules),
                result.getPageable(),
                filteredModules.size());
        return PageResponse.from(pageDto);
    }

    @Override
    public AuthContextResponse getContextForCurrentUser() {
        String username = jwtUtil.getCurrentUser();
        User user = getAuthUser(username);
        List<String> roles = authUserRoleRepository.findActiveRoleNamesByUserId(user.getId());
        List<UUID> roleIds = authUserRoleRepository.findActiveRoleIdsByUserId(user.getId());
        List<AuthModule> modules = getEnabledModules(user, roleIds);

        // The request token may represent a previous auth state (e.g. before role/license updates).
        // We always emit a fresh context token to keep frontend UX claims in sync with backend policy.
        AuthArtifacts authArtifacts = buildAuthArtifacts(user);

        return AuthContextResponse.builder()
                .user(AuthContextUserDto.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .status(user.getStatus() != null ? user.getStatus().getId() : null)
                        .build())
                .tenant(buildTenantDto(user))
                .roles(roles)
                .modules(toContextModules(user, modules))
                .token(AuthContextTokenDto.builder()
                        .accessToken(authArtifacts.token())
                        .expiresAt(authArtifacts.expiresAt())
                        .build())
                .serverTime(Instant.now())
                .build();
    }

    private AuthContextTenantDto buildTenantDto(User user) {
        String tenantId = null;
        String tenantName = null;
        Integer tenantStatus = null;

        if (user.getTenant() != null) {
            tenantId = user.getTenant().getId().toString();
            tenantName = user.getTenant().getName();
            tenantStatus = user.getTenant().getStatus();
        }

        if (tenantId == null) {
            // TODO: while migrating older accounts, tenant can still be inferred dynamically during login.
            String contextTenantId = TenantContext.getTenantId();
            tenantId = contextTenantId;
            if (contextTenantId != null) {
                Optional<Tenant> tenant = tenantRepository.findById(UUID.fromString(contextTenantId));
                if (tenant.isPresent()) {
                    tenantName = tenant.get().getName();
                    tenantStatus = tenant.get().getStatus();
                }
            }
        }

        return AuthContextTenantDto.builder()
                .tenantId(tenantId)
                .name(tenantName)
                .status(tenantStatus)
                .build();
    }

    private List<AuthContextModuleDto> toContextModules(User user, List<AuthModule> modules) {
        UUID tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        Map<UUID, ModuleLicense> licensesByModuleId = tenantId == null
                ? Map.of()
                : moduleLicenseRepository.findAllByTenantIdWithModule(tenantId).stream()
                .filter(license -> license.getModule() != null)
                .collect(Collectors.toMap(license -> license.getModule().getId(), Function.identity(), (a, b) -> a));

        return modules.stream()
                .map(module -> AuthContextModuleDto.builder()
                        .key(ModuleKeyNormalizer.normalize(module.getName()))
                        .label(module.getName())
                        .enabled(Boolean.TRUE)
                        .expiresAt(resolveLicenseExpiration(licensesByModuleId.get(module.getId())))
                        .build())
                .sorted(Comparator.comparing(AuthContextModuleDto::getKey, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private OffsetDateTime resolveLicenseExpiration(ModuleLicense license) {
        if (license == null) {
            return null;
        }
        if (license.getEndAt() != null) {
            return license.getEndAt();
        }
        return license.getExpiresAt();
    }

    private List<AuthModule> getEnabledModules(User user, List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        UUID tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        return roleModuleRepository.findDistinctModulesByRoleIds(roleIds).stream()
                .filter(module -> tenantId == null || featureFlagService.isModuleEnabled(tenantId, module.getName()))
                .toList();
    }

    private AuthArtifacts buildAuthArtifacts(User user) {
        List<String> roles = authUserRoleRepository.findActiveRoleNamesByUserId(user.getId());
        List<UUID> roleIds = authUserRoleRepository.findActiveRoleIdsByUserId(user.getId());
        List<String> moduleKeys = getEnabledModules(user, roleIds).stream()
                .map(AuthModule::getName)
                .map(ModuleKeyNormalizer::normalize)
                .toList();

        String token = jwtUtil.generateToken(user, roles, moduleKeys);
        return new AuthArtifacts(token, jwtUtil.extractExpiration(token));
    }


    private Tenant resolveOrCreateTenant(String username) {
        String contextTenantId = TenantContext.getTenantId();
        if (contextTenantId != null && !contextTenantId.isBlank()) {
            return tenantRepository.findById(UUID.fromString(contextTenantId))
                    .orElseThrow(() -> new NotFoundException("Tenant not found"));
        }

        Tenant tenant = Tenant.builder()
                .name("tenant-" + username)
                .status(1)
                .build();
        return tenantRepository.save(tenant);
    }

    private record AuthArtifacts(String token, Instant expiresAt) {
    }
}
