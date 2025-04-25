package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.auth.request.LoginRequest;
import com.testtask.bankcardmanager.dto.auth.response.JwtAuthenticationResponse;
import com.testtask.bankcardmanager.security.jwt.JwtTokenProvider;
import com.testtask.bankcardmanager.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        logger.info("Attempting authentication for user: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        logger.debug("Authentication successful for user: {}", loginRequest.getEmail());

        String jwt = tokenProvider.generateToken(authentication);
        logger.debug("JWT generated successfully for user: {}", loginRequest.getEmail());

        return new JwtAuthenticationResponse(jwt);
    }
}
