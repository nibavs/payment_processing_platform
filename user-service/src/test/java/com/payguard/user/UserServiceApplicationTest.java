package com.payguard.user;

import com.payguard.user.dto.AuthResponse;
import com.payguard.user.dto.LoginRequest;
import com.payguard.user.dto.RegisterRequest;
import com.payguard.user.dto.UserResponse;
import com.payguard.user.entity.User;
import com.payguard.user.enums.Role;
import com.payguard.user.exception.EmailAlreadyExistsException;
import com.payguard.user.exception.InvalidCredentialsException;
import com.payguard.user.mapper.UserMapper;
import com.payguard.user.repository.UserRepository;
import com.payguard.user.security.JwtTokenProvider;
import com.payguard.user.service.EventPublisher;
import com.payguard.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = RegisterRequest.builder()
                .email("shop@test.com")
                .password("securePass123")
                .merchantName("Shop Inc.")
                .merchantCategory("retail")
                .country("USA")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("shop@test.com")
                .passwordHash("hashed_password")
                .merchantName("Shop Inc.")
                .merchantCategory("retail")
                .country("USA")
                .role(Role.MERCHANT)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .merchantName(savedUser.getMerchantName())
                .merchantCategory(savedUser.getMerchantCategory())
                .country(savedUser.getCountry())
                .role(savedUser.getRole())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        when(jwtTokenProvider.generateToken(any(User.class)))
                .thenReturn("jwt_token_123");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
        doNothing().when(eventPublisher).publishUserRegistered(any(User.class));

        AuthResponse response = userService.register(request);

        assertThat(response.token()).isEqualTo("jwt_token_123");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userResponse().email()).isEqualTo("shop@test.com");
        assertThat(response.userResponse().role()).isEqualTo(Role.MERCHANT);

        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishUserRegistered(any(User.class));
        verify(passwordEncoder).encode("securePass123");
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@test.com")
                .password("securePass123")
                .merchantName("Existing Store")
                .merchantCategory("retail")
                .country("USA")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishUserRegistered(any());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("shop@test.com")
                .password("securePass123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("shop@test.com")
                .passwordHash("hashed_password")
                .merchantName("Shop Inc.")
                .merchantCategory("retail")
                .country("USA")
                .role(Role.MERCHANT)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .merchantName(user.getMerchantName())
                .merchantCategory(user.getMerchantCategory())
                .country(user.getCountry())
                .role(user.getRole())
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("securePass123", "hashed_password")).thenReturn(true);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        when(jwtTokenProvider.generateToken(any(User.class)))
                .thenReturn("jwt_token_456");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = userService.login(request);

        assertThat(response.token()).isEqualTo("jwt_token_456");
        assertThat(response.userResponse().email()).isEqualTo("shop@test.com");
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("shop@test.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("shop@test.com")
                .passwordHash("hashed_password")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowForNonExistentEmail() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("nobody@test.com")
                .password("anyPassword")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

}