package com.kubrafelek.brokagefirm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "assets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "asset_name"})
})
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Asset name is required")
    @Column(name = "asset_name")
    private String assetName;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Size must be non-negative")
    @Column(precision = 19, scale = 2)
    private BigDecimal size;

    @NotNull(message = "Usable size is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Usable size must be non-negative")
    @Column(name = "usable_size", precision = 19, scale = 2)
    private BigDecimal usableSize;

    public Asset() {}

    public Asset(Long userId, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.userId = userId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }

    public String getAssetName() {
        return assetName;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getUsableSize() {
        return usableSize;
    }

    public void setUsableSize(BigDecimal usableSize) {
        this.usableSize = usableSize;
    }
}
