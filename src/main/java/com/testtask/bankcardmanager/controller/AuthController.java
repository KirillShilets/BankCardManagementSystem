package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.auth.request.LoginRequest;
import com.testtask.bankcardmanager.dto.auth.response.JwtAuthenticationResponse;
import com.testtask.bankcardmanager.dto.response.ErrorResponse;
import com.testtask.bankcardmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Контроллер для аутентификации пользователей")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Аутентификация пользователя", description = "Принимает email и пароль, возвращает JWT токен доступа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные входные данные (например, пустой email/пароль)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad Request", summary = "Невалидные данные", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:14:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Request body validation failed. Check 'validationErrors' for details.",
                                      "path": "/api/auth/login",
                                      "validationErrors": { "password": ["Password cannot be blank"] }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Ошибка аутентификации (неверный email/пароль)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Неверные креды", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:15:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Bad credentials",
                                      "path": "/api/auth/login",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}