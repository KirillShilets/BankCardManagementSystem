package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.Role;
import jakarta.validation.constraints.Email;

public class GetUsersRequest {

    @Email(message = "Email should be valid if provided")
    private String email;

    private Role role;

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