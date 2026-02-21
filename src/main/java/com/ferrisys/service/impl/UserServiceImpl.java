package com.ferrisys.service.impl;

import com.ferrisys.common.dto.ModuleDTO;
import com.ferrisys.common.dto.PageResponse;
import com.ferrisys.common.entity.user.AuthModule;
import com.ferrisys.mapper.ModuleMapper;
import com.ferrisys.repository.AuthUserRoleRepository;
import com.ferrisys.repository.RoleModuleRepository;
import com.ferrisys.repository.RoleRepository;
import com.ferrisys.repository.UserRepository;
import com.ferrisys.repository.UserStatusRepository;
import com.ferrisys.service.FeatureFlagService;
import com.ferrisys.service.UserService;
import com.ferrisys.common.dto.AuthResponse;
import com.ferrisys.common.dto.RegisterRequest;
import com.ferrisys.common.entity.user.Role;
import com.ferrisys.common.entity.user.AuthUserRole;
import com.ferrisys.common.entity.user.User;
import com.ferrisys.common.entity.user.UserStatus;
import com.ferrisys.config.security.JWTUtil;
import com.ferrisys.common.exception.impl.BadRequestException;
import com.ferrisys.common.exception.impl.NotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ferrisys.common.enums.DefaultRole;
import com.ferrisys.common.enums.DefaultUserStatus;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthUserRoleRepository authUserRoleRepository;
    private final RoleRepository roleRepository ;
    private final UserStatusRepository userStatusRepository ;
    private final RoleModuleRepository roleModuleRepository;
    private final JWTUtil jwtUtil;
    private final FeatureFlagService featureFlagService;
    private final ModuleMapper moduleMapper;

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

        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
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
                .orElseThrow(() -> new RuntimeException("Estado de usuario no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(new BCryptPasswordEncoder().encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(activeStatus)
                .build();

        User saved = userRepository.save(user);

        Role defaultRole = roleRepository.findById(DefaultRole.USER.getId())
                .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));

        AuthUserRole userRole = AuthUserRole.builder()
                .user(saved)
                .role(defaultRole)
                .status(1)
                .build();

        authUserRoleRepository.save(userRole);

        String token = jwtUtil.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .username(saved.getUsername())
                .email(saved.getEmail())
                .role(defaultRole.getName())
                .build();
    }

    @Override
    public AuthResponse authenticate(String username, String password) {
        User user = getAuthUser(username);

        if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        AuthUserRole role = getUserRole(user.getId());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(role.getRole().getName())
                .build();
    }

    @Override
    public AuthResponse recoverPassword(String newPassword, String confirmPassword, String userToken) {
        Claims claims = jwtUtil.getClaims(userToken);
        String username = claims.getSubject();

        User user = getUserByUsername(username);
        changePassword(user.getId(), newPassword, confirmPassword);

        String token = jwtUtil.generateToken(user);
        AuthUserRole role = getUserRole(user.getId());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
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
                .filter(module -> featureFlagService.enabled(user.getId(), module.getName()))
                .toList();
        Page<ModuleDTO> pageDto = new PageImpl<>(
                moduleMapper.toDtoList(filteredModules),
                result.getPageable(),
                result.getTotalElements());
        return PageResponse.from(pageDto);
    }

}
