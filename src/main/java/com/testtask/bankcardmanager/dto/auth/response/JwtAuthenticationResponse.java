package com.testtask.bankcardmanager.dto.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с JWT токеном после успешной аутентификации")
public class JwtAuthenticationResponse {

    @Schema(description = "Токен доступа JWT", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNz...", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;

    @Schema(description = "Тип токена", example = "Bearer", accessMode = Schema.AccessMode.READ_ONLY)
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}