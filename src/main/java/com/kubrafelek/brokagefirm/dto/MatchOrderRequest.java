package com.kubrafelek.brokagefirm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for matching an order")
public class MatchOrderRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the order to match", example = "1")
    private Long orderId;

    public MatchOrderRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
