package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.auth.request.LoginRequest;
import com.testtask.bankcardmanager.dto.auth.response.JwtAuthenticationResponse;

public interface AuthService {
    JwtAuthenticationResponse login(LoginRequest loginRequest);
}
