package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = SwaggerConstants.Tags.API_INFO_NAME, description = SwaggerConstants.Tags.API_INFO_DESC)
public class ApiInfoController {

    @GetMapping("/")
    @Operation(summary = SwaggerConstants.OperationSummaries.API_INFORMATION,
            description = SwaggerConstants.OperationDescriptions.API_INFO_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.ResponseCodes.OK, description = SwaggerConstants.ResponseDescriptions.API_INFO_RETRIEVED_SUCCESSFULLY)
    })
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Brokerage Firm Backend API");
        response.put("version", "1.0.0");
        response.put("status", "Running");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Login", "POST /api/auth/login");
        endpoints.put("Create Order", "POST /api/orders");
        endpoints.put("List Orders", "GET /api/orders");
        endpoints.put("Cancel Order", "DELETE /api/orders/{orderId}");
        endpoints.put("List Assets", "GET /api/assets");
        endpoints.put("Match Order (Admin)", "POST /api/orders/match");
        endpoints.put("List Pending Orders (Admin)", "GET /api/orders/pending");
        endpoints.put("H2 Console", "GET /h2-console");
        endpoints.put("Swagger UI", "GET /swagger-ui.html");
        endpoints.put("API Docs", "GET /api-docs");

        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = SwaggerConstants.OperationSummaries.HEALTH_CHECK,
            description = SwaggerConstants.OperationDescriptions.HEALTH_CHECK_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.ResponseCodes.OK, description = SwaggerConstants.ResponseDescriptions.SERVICE_HEALTHY)
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
