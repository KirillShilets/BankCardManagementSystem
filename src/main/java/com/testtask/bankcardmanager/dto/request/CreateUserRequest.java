package com.testtask.bankcardmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Запрос на создание нового пользователя")
public class CreateUserRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email is too long")
    @Schema(description = "Email пользователя (должен быть уникальным)", requiredMode = Schema.RequiredMode.REQUIRED, example = "newuser@example.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,100}$",
            message = "Password must be 8-100 characters long and include at least one digit, one lowercase letter, one uppercase letter, and one special character (!@#$%^&*)")
    @Schema(description = "Пароль пользователя (требования: 8-100 символов, цифры, буквы верх/низ, спецсимволы !@#$%^&*)",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "StrongP@ssw0rd!", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}