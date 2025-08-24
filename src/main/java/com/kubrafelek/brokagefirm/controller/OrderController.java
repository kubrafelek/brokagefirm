package com.kubrafelek.brokagefirm.controller;

import com.kubrafelek.brokagefirm.constants.Constants;
import com.kubrafelek.brokagefirm.dto.CreateOrderRequest;
import com.kubrafelek.brokagefirm.dto.MatchOrderRequest;
import com.kubrafelek.brokagefirm.entity.User;
import com.kubrafelek.brokagefirm.entity.Order;
import com.kubrafelek.brokagefirm.service.UserService;
import com.kubrafelek.brokagefirm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = Constants.Tags.ORDERS_NAME, description = Constants.Tags.ORDERS_DESC)
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = Constants.OperationSummaries.CREATE_NEW_ORDER, description = Constants.OperationDescriptions.CREATE_ORDER_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.ORDER_CREATED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.BAD_REQUEST, description = Constants.ResponseDescriptions.INVALID_ORDER_DATA),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.FORBIDDEN, description = Constants.ResponseDescriptions.NOT_AUTHORIZED_CREATE_ORDER),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> createOrder(
            @Parameter(description = Constants.ParameterDescriptions.ORDER_CREATION_REQUEST, required = true) @Valid @RequestBody CreateOrderRequest request,
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true) @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true) @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            // Only admin users can create orders for any customer
            if (!user.isAdmin()) {
                return ResponseEntity.status(403).body(Constants.ErrorMessages.ONLY_ADMIN_USERS_CAN_CREATE_ORDERS);
            }

            Order order = orderService.createOrder(
                    request.getUserId(),
                    request.getAssetName(),
                    request.getSide(),
                    request.getSize(),
                    request.getPrice());

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Constants.ErrorMessages.FAILED_TO_CREATE_ORDER + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = Constants.OperationSummaries.LIST_ORDERS, description = Constants.OperationDescriptions.LIST_ORDERS_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.ORDERS_RETRIEVED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> listOrders(
            @Parameter(description = "User ID to filter orders (admin only)") @RequestParam(required = false) Long userId,
            @Parameter(description = Constants.ParameterDescriptions.START_DATE_FILTER) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = Constants.ParameterDescriptions.END_DATE_FILTER) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true) @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true) @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            List<Order> orders;

            if (user.isAdmin()) {
                if (userId != null) {
                    if (startDate != null && endDate != null) {
                        orders = orderService.getOrdersByUserIdAndDateRange(userId, startDate, endDate);
                    } else {
                        orders = orderService.getOrdersByUserId(userId);
                    }
                } else {
                    if (startDate != null && endDate != null) {
                        orders = orderService.getAllOrdersByDateRange(startDate, endDate);
                    } else {
                        orders = orderService.getAllOrders();
                    }
                }
            } else {
                Long targetUserId = user.getId();
                if (startDate != null && endDate != null) {
                    orders = orderService.getOrdersByUserIdAndDateRange(targetUserId, startDate, endDate);
                } else {
                    orders = orderService.getOrdersByUserId(targetUserId);
                }
            }

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Constants.ErrorMessages.FAILED_TO_LIST_ORDERS + e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = Constants.OperationSummaries.CANCEL_ORDER, description = Constants.OperationDescriptions.CANCEL_ORDER_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.ORDER_CANCELLED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.BAD_REQUEST, description = Constants.ResponseDescriptions.ORDER_CANNOT_BE_CANCELLED),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.NOT_FOUND, description = Constants.ResponseDescriptions.ORDER_NOT_FOUND),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = Constants.ParameterDescriptions.ORDER_ID_TO_CANCEL, required = true) @PathVariable Long orderId,
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true) @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true) @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            Order cancelledOrder = orderService.cancelOrder(orderId, user.getId(), user.isAdmin());
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Constants.ErrorMessages.FAILED_TO_CANCEL_ORDER + e.getMessage());
        }
    }

    @PostMapping("/match")
    @Operation(summary = Constants.OperationSummaries.MATCH_ORDER, description = Constants.OperationDescriptions.MATCH_ORDER_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.ORDER_MATCHED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.BAD_REQUEST, description = Constants.ResponseDescriptions.ORDER_CANNOT_BE_MATCHED),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.FORBIDDEN, description = Constants.ResponseDescriptions.ONLY_ADMIN_MATCH_ORDERS),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> matchOrder(
            @Parameter(description = Constants.ParameterDescriptions.MATCH_ORDER_REQUEST, required = true) @Valid @RequestBody MatchOrderRequest request,
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true) @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true) @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            // Only admin users can match orders
            if (!user.isAdmin()) {
                // Use the explicit admin-match constant for consistency
                return ResponseEntity.status(403).body(Constants.ErrorMessages.ONLY_ADMIN_MATCH_ORDERS);
            }

            Order matchedOrder = orderService.matchOrder(request.getOrderId());
            return ResponseEntity.ok(matchedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Constants.ErrorMessages.FAILED_TO_MATCH_ORDER + e.getMessage());
        }
    }

    @GetMapping("/pending")
    @Operation(summary = Constants.OperationSummaries.LIST_PENDING_ORDERS, description = Constants.OperationDescriptions.LIST_PENDING_ORDERS_DESC)
    @ApiResponses(value = {
            @ApiResponse(responseCode = Constants.ResponseCodes.OK, description = Constants.ResponseDescriptions.PENDING_ORDERS_RETRIEVED_SUCCESSFULLY),
            @ApiResponse(responseCode = Constants.ResponseCodes.UNAUTHORIZED, description = Constants.ResponseDescriptions.INVALID_CREDENTIALS),
            @ApiResponse(responseCode = Constants.ResponseCodes.FORBIDDEN, description = Constants.ResponseDescriptions.ONLY_ADMIN_VIEW_PENDING_ORDERS),
            @ApiResponse(responseCode = Constants.ResponseCodes.INTERNAL_SERVER_ERROR, description = Constants.ResponseDescriptions.INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<?> listPendingOrders(
            @Parameter(description = Constants.ParameterDescriptions.USERNAME_AUTH, required = true) @RequestHeader("Username") String username,
            @Parameter(description = Constants.ParameterDescriptions.PASSWORD_AUTH, required = true) @RequestHeader("Password") String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user == null) {
                return ResponseEntity.status(401).body(Constants.ErrorMessages.INVALID_CREDENTIALS);
            }

            if (!user.isAdmin()) {
                return ResponseEntity.status(403)
                        .body(Constants.ErrorMessages.ONLY_ADMIN_USERS_CAN_VIEW_PENDING_ORDERS);
            }

            List<Order> pendingOrders = orderService.getPendingOrders();
            return ResponseEntity.ok(pendingOrders);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Constants.ErrorMessages.FAILED_TO_LIST_PENDING_ORDERS + e.getMessage());
        }
    }
}
