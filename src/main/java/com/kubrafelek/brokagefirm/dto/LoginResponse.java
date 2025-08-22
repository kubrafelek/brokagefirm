package com.kubrafelek.brokagefirm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object for user login")
public class LoginResponse {

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "Customer ID", example = "2")
    private Long customerId;

    @Schema(description = "Whether the user is an admin", example = "false")
    private Boolean isAdmin;

    public LoginResponse(String message, Long customerId, Boolean isAdmin) {
        this.message = message;
        this.customerId = customerId;
        this.isAdmin = isAdmin;
    }

    public String getMessage() {
        return message;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }
}
