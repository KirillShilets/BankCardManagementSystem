package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.Role;

public class UserResponse {
    private Long id;
    private String email;
    private Role role;

    public UserResponse(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
