package com.payguard.user.service;

import com.payguard.shared.notification.NotificationChannel;
import com.payguard.user.dto.AuthResponse;
import com.payguard.user.dto.LoginRequest;
import com.payguard.user.dto.RegisterRequest;
import com.payguard.user.entity.User;
import com.payguard.user.enums.Role;
import com.payguard.user.exception.EmailAlreadyExistsException;
import com.payguard.user.exception.InvalidCredentialsException;
import com.payguard.user.exception.PhoneNumberAlreadyExistsException;
import com.payguard.user.mapper.UserMapper;
import com.payguard.user.repository.UserRepository;
import com.payguard.user.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EventPublisher eventPublisher;
    private final S3StorageService s3StorageService;

    @Transactional
    public AuthResponse register(@Valid RegisterRequest request, MultipartFile photo) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email().trim().toLowerCase());
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new PhoneNumberAlreadyExistsException(request.phoneNumber());
        }
        String avatarKey = photo == null ? null : s3StorageService.upload(photo);

        User user = User.builder()
                .email(request.email().trim().toLowerCase())
                .phoneNumber(request.phoneNumber())
                .preferableNotificationChannel(request.preferableNotificationChannel() == null
                        ? NotificationChannel.EMAIL
                        : request.preferableNotificationChannel()
                )
                .passwordHash(passwordEncoder.encode(request.password()))
                .merchantName(request.merchantName().trim())
                .merchantCategory(request.merchantCategory().trim().toLowerCase())
                .country(request.country().trim().toUpperCase())
                .role(Role.MERCHANT)
                .avatarKey(avatarKey)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New merchant registered: {} ({})", savedUser.getEmail(), savedUser.getId());

        eventPublisher.publishUserRegistered(savedUser);

        String generatedToken = jwtTokenProvider.generateToken(savedUser);

        String avatarUrl = s3StorageService.getPhotoUrl(avatarKey);

        return AuthResponse.builder()
                .token(generatedToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .userResponse(userMapper.toResponse(savedUser, avatarUrl))
                .build();
    }

    public AuthResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("Merchant logged in: {} ({})", user.getEmail(), user.getId());

        String generatedToken = jwtTokenProvider.generateToken(user);

        String avatarUrl = s3StorageService.getPhotoUrl(user.getAvatarKey());

        return AuthResponse.builder()
                .token(generatedToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .userResponse(userMapper.toResponse(user, avatarUrl))
                .build();
    }
}
