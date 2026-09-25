package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.LoginRequest;
import com.duong.issue_tracker.dto.request.RegisterRequest;
import com.duong.issue_tracker.dto.response.LoginResponse;
import com.duong.issue_tracker.dto.response.UserResponse;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.Role;
import com.duong.issue_tracker.exception.DuplicateResourceException;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.UserRepository;
import com.duong.issue_tracker.util.JwtUtil;
import com.duong.issue_tracker.util.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationMs;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedUsername = TextNormalizer.username(request.username());
        String normalizedFullName = TextNormalizer.compact(request.fullName());
        String normalizedEmail = TextNormalizer.email(request.email());

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        String normalizedUsername = TextNormalizer.username(request.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedUsername,
                        request.password()
                )
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return new LoginResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        return getByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        String normalizedUsername = TextNormalizer.username(username);
        User user = userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + normalizedUsername));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

}
