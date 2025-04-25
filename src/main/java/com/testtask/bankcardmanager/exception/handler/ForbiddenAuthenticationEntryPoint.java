package com.testtask.bankcardmanager.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtask.bankcardmanager.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class ForbiddenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(ForbiddenAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public ForbiddenAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.warn("Authentication required but Forbidden response requested for path: {}. Reason: {}",
                request.getRequestURI(), authException.getMessage());
        logger.debug("AuthenticationException details: ", authException);


        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access Denied: Authentication is required and/or you do not have permissions.",
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try (OutputStream os = response.getOutputStream()) {
            objectMapper.writeValue(os, errorResponse);
            os.flush();
        }
    }
}