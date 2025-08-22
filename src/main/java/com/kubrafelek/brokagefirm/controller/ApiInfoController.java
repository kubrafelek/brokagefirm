package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.Constants;
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
@Tag(name = Constants.Tags.API_INFO_NAME, description = Constants.Tags.API_INFO_DESC)
public class ApiInfoController {

    @GetMapping("/health")
    @Operation(summary = Constants.OperationSummaries.HEALTH_CHECK,
            description = Constants.OperationDescriptions.HEALTH_CHECK_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.SERVICE_HEALTHY)
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
