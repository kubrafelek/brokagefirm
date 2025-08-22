package com.kubrafelek.brokagefirm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object for user login")
public class LoginResponse {

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "User ID", example = "2")
    private Long userId;

    @Schema(description = "Whether the user is an admin", example = "false")
    private Boolean isAdmin;

    public LoginResponse(String message, Long userId, Boolean isAdmin) {
        this.message = message;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
