package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.Role;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;

public class GetUsersRequest {

    @Email(message = "Email should be valid if provided")
    @Parameter(description = "Фильтр по email пользователя (частичное совпадение, регистронезависимое)")
    private String email;

    @Parameter(description = "Фильтр по роли пользователя (ROLE_ADMIN, ROLE_USER)")
    private Role role;

    @Parameter(description = "Фильтр по статусу блокировки аккаунта (true - заблокирован, false - не заблокирован)")
    private Boolean locked;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "GetUsersRequest{" +
                "email='" + email + '\'' +
                ", role=" + role +
                ", locked=" + locked +
                '}';
    }
}