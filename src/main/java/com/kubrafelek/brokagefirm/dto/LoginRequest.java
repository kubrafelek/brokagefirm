package com.kubrafelek.brokagefirm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for user login")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username for authentication", example = "customer1")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for authentication", example = "pass123")
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
