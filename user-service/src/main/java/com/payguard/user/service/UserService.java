package com.payguard.user.service;

import com.payguard.user.dto.AuthResponse;
import com.payguard.user.dto.LoginRequest;
import com.payguard.user.dto.RegisterRequest;
import com.payguard.user.entity.User;
import com.payguard.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse register(@Valid RegisterRequest request) {
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .merchantName(request.merchantName())
                .merchantCategory(request.merchantCategory())
                .country(request.country())
                .role(User.Role.MERCHANT)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New merchant registered: {} ({})", savedUser.getEmail(), savedUser.getId());

        // TODO: Publish event to Kafka for welcome notification sending

        // TODO: Generate token

        // TODO: Build and send back AuthResponse

    }

    public AuthResponse login(@Valid LoginRequest request) {
        return null;
    }
}
