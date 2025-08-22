package com.kubrafelek.brokagefirm.dto;

import com.kubrafelek.brokagefirm.enums.OrderSide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer placing the order", example = "2")
    private Long customerId;

    @NotBlank(message = "Asset name is required")
    @Schema(description = "Name of the asset to trade", example = "AAPL")
    private String assetName;

    @NotNull(message = "Order side is required")
    @Schema(description = "Order side - BUY or SELL", example = "BUY")
    private OrderSide side;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0")
    @Schema(description = "Number of shares to trade", example = "5.00")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Price per share", example = "150.00")
    private BigDecimal price;

    public CreateOrderRequest(Long customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        this.customerId = customerId;
        this.assetName = assetName;
        this.side = side;
        this.size = size;
        this.price = price;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
}
