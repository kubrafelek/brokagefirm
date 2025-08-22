package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.SwaggerConstants;
import com.kubrafelek.brokagefirm.dto.LoginRequest;
import com.kubrafelek.brokagefirm.dto.LoginResponse;
import com.kubrafelek.brokagefirm.entity.Customer;
import com.kubrafelek.brokagefirm.service.CustomerService;
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
@Tag(name = SwaggerConstants.Tags.AUTHENTICATION_NAME, description = SwaggerConstants.Tags.AUTHENTICATION_DESC)
public class AuthController {

    private final CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/login")
    @Operation(summary = SwaggerConstants.OperationSummaries.USER_LOGIN,
               description = SwaggerConstants.OperationDescriptions.USER_LOGIN_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.ResponseCodes.OK, description = SwaggerConstants.ResponseDescriptions.LOGIN_SUCCESSFUL),
            @ApiResponse(responseCode = SwaggerConstants.ResponseCodes.UNAUTHORIZED, description = SwaggerConstants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = SwaggerConstants.ResponseCodes.INTERNAL_SERVER_ERROR, description = SwaggerConstants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = SwaggerConstants.ParameterDescriptions.LOGIN_CREDENTIALS, required = true)
            @Valid @RequestBody LoginRequest request) {
        try {
            Customer customer = customerService.authenticate(request.getUsername(), request.getPassword());
            if (customer != null) {
                LoginResponse response = new LoginResponse(
                    SwaggerConstants.SuccessMessages.LOGIN_SUCCESSFUL,
                    customer.getId(),
                    customer.getIsAdmin()
                );
                return ResponseEntity.ok(response);
            } else {
                LoginResponse response = new LoginResponse(SwaggerConstants.ErrorMessages.INVALID_CREDENTIALS, null, null);
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            LoginResponse response = new LoginResponse(SwaggerConstants.ErrorMessages.LOGIN_FAILED + e.getMessage(), null, null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
