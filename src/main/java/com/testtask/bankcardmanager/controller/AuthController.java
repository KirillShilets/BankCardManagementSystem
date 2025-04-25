package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.auth.request.LoginRequest;
import com.testtask.bankcardmanager.dto.auth.response.JwtAuthenticationResponse;
import com.testtask.bankcardmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
