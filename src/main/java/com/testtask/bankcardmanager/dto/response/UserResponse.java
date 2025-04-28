package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с информацией о пользователе")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Email пользователя", example = "user@example.com", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    @Schema(description = "Роль пользователя", example = "ROLE_USER", accessMode = Schema.AccessMode.READ_ONLY)
    private Role role;

    public UserResponse(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}