package com.duong.issue_tracker.service;

import com.duong.issue_tracker.dto.request.RegisterRequest;
import com.duong.issue_tracker.dto.request.LoginRequest;
import com.duong.issue_tracker.dto.response.LoginResponse;
import com.duong.issue_tracker.dto.response.UserResponse;
import com.duong.issue_tracker.entity.User;
import com.duong.issue_tracker.enums.Role;
import com.duong.issue_tracker.exception.DuplicateResourceException;
import com.duong.issue_tracker.exception.ResourceNotFoundException;
import com.duong.issue_tracker.repository.UserRepository;
import com.duong.issue_tracker.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldCreateUser_whenInputIsValid() {
        RegisterRequest request = new RegisterRequest(
                "duong",
                "Duong Tran",
                "duong@example.com",
                "Password123!"
        );

        when(userRepository.existsByUsername("duong")).thenReturn(false);
        when(userRepository.existsByEmail("duong@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.register(request);

        assertThat(response.username()).isEqualTo("duong");
        assertThat(response.email()).isEqualTo("duong@example.com");
        assertThat(response.fullName()).isEqualTo("Duong Tran");
        assertThat(response.role()).isEqualTo(Role.USER.name());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldTrimWhitespaceFromUserInput() {
        RegisterRequest request = new RegisterRequest(
                "  duong  ",
                "  Duong   Tran   ",
                "   duong@example.com   ",
                " Password123! "
        );

        when(userRepository.existsByUsername("duong")).thenReturn(false);
        when(userRepository.existsByEmail("duong@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.register(request);

        assertThat(response.username()).isEqualTo("duong");
        assertThat(response.fullName()).isEqualTo("Duong Tran");
        assertThat(response.email()).isEqualTo("duong@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_shouldPreserveWhitespaceInsidePassword() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getName()).thenReturn("duong");
        when(jwtUtil.generateToken("duong")).thenReturn("token");

        LoginResponse response = userService.login(new LoginRequest("  duong  ", " pass  word "));

        assertThat(response.accessToken()).isEqualTo("token");
        org.mockito.ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                org.mockito.ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getCredentials()).isEqualTo("pass  word");
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "duong",
                "Duong Tran",
                "duong@example.com",
                "Password123!"
        );

        when(userRepository.existsByUsername("duong")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
    }

    @Test
    void getProfile_shouldReturnCurrentUserInfo() {
        User user = new User();
        user.setId(7L);
        user.setUsername("duong");
        user.setFullName("Duong Tran");
        user.setEmail("duong@example.com");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("duong")).thenReturn(java.util.Optional.of(user));

        UserResponse response = userService.getProfile("duong");

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("duong");
        assertThat(response.email()).isEqualTo("duong@example.com");
        assertThat(response.role()).isEqualTo(Role.USER.name());
    }

    @Test
    void getByUsername_shouldReturnMatchingUserProfile() {
        User user = new User();
        user.setId(9L);
        user.setUsername("alice");
        user.setFullName("Alice Nguyen");
        user.setEmail("alice@example.com");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));

        UserResponse response = userService.getByUsername("alice");

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.fullName()).isEqualTo("Alice Nguyen");
    }

    @Test
    void getByUsername_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("missing"));
    }
}
