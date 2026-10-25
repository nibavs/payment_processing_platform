package com.payguard.user.controller;

import com.payguard.user.dto.AuthResponse;
import com.payguard.user.dto.LoginRequest;
import com.payguard.user.dto.RegisterRequest;
import com.payguard.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo
            ) {
        AuthResponse response = userService.register(request, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}
