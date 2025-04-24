package com.testtask.bankcardmanager.dto.request;

import jakarta.validation.constraints.*;

public class CreateUserRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email is too long")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,100}$",
            message = "Password must be 8-100 characters long and include at least one digit, one lowercase letter, one uppercase letter, and one special character (!@#$%^&*)")
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
