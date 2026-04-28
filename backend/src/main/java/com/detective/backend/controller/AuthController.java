package com.detective.backend.controller;

import com.detective.backend.dto.AuthResetRequest;
import com.detective.backend.dto.AuthResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.detective.backend.dto.AuthRequest;
import com.detective.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody AuthResetRequest request) {
        authService.resetPassword(request.getUsername(), request.getNewPassword());
        return ResponseEntity.ok("Password successfully reset.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Successfully logged out (client should drop token now).");
    }
}
