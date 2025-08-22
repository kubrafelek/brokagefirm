package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.Constants;
import com.kubrafelek.brokagefirm.dto.LoginRequest;
import com.kubrafelek.brokagefirm.dto.LoginResponse;
import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = Constants.Tags.AUTHENTICATION_NAME, description = Constants.Tags.AUTHENTICATION_DESC)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = Constants.OperationSummaries.USER_LOGIN,
               description = Constants.OperationDescriptions.USER_LOGIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.LOGIN_SUCCESSFUL),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = Constants.ParameterDescriptions.LOGIN_CREDENTIALS, required = true)
            @Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getUsername(), request.getPassword());
            if (user != null) {
                LoginResponse response = new LoginResponse(
                    Constants.SuccessMessages.LOGIN_SUCCESSFUL,
                    user.getId(),
                    user.isAdmin()
                );
                return ResponseEntity.ok(response);
            } else {
                LoginResponse response = new LoginResponse(Constants.ErrorMessages.INVALID_CREDENTIALS, null, null);
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            LoginResponse response = new LoginResponse(Constants.ErrorMessages.LOGIN_FAILED + e.getMessage(), null, null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
