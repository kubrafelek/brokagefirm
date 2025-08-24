package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.Constants;
import com.kubrafelek.brokagefirm.entity.Asset;
import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.service.AssetService;
import com.kubrafelek.brokagefirm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@Tag(name = Constants.Tags.ASSETS_NAME, description = Constants.Tags.ASSETS_DESC)
public class AssetController {

    private final AssetService assetService;
    private final UserService userService;

    public AssetController(AssetService assetService, UserService userService) {
        this.assetService = assetService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = Constants.OperationSummaries.LIST_CUSTOMER_ASSETS,
               description = Constants.OperationDescriptions.LIST_ASSETS_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.ASSETS_RETRIEVED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.BAD_REQUEST, description = Constants.ResponseDescriptions.CUSTOMER_ID_REQUIRED_ADMIN),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> listAssets(
            @Parameter(description = "User ID (required for admin users, ignored for regular customers)")
            @RequestParam(required = false) Long userId,
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true)
            @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true)
            @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            List<Asset> assets;

            if (user.isAdmin()) {
                if (userId != null) {
                    assets = assetService.getAssetsByUserId(userId);
                } else {
                    return ResponseEntity.status(400).body(Constants.ErrorMessages.CUSTOMER_ID_REQUIRED_FOR_ADMIN);
                }
            } else {
                assets = assetService.getAssetsByUserId(user.getId());
            }

            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Constants.ErrorMessages.FAILED_TO_LIST_ASSETS + e.getMessage());
        }
    }
}