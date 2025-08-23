package com.kubrafelek.brokagefirm.dto;

import com.kubrafelek.brokagefirm.enums.OrderSide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user placing the order", example = "2")
    private Long userId;

    @NotBlank(message = "Asset name must be a non-blank string")
    @Schema(description = "Name of the asset to trade", example = "AAPL")
    private String assetName;

    @NotNull(message = "Order side is required and must be either BUY or SELL")
    @Schema(description = "Order side - BUY or SELL", example = "BUY")
    private OrderSide side;

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be a positive number greater than zero")
    @Schema(description = "Number of shares to trade", example = "5.00")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive number greater than zero")
    @Schema(description = "Price per share", example = "150.00")
    private BigDecimal price;

    @Schema(description = "Timestamp when the order was created", example = "2023-10-05T14:48:00")
    private LocalDateTime createdAt;


    public CreateOrderRequest(Long userId, String assetName, OrderSide side, BigDecimal size, BigDecimal price, LocalDateTime createdAt) {
        this.userId = userId;
        this.assetName = assetName;
        this.side = side;
        this.size = size;
        this.price = price;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAssetName() {
        return assetName;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getSize() {
        return size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
