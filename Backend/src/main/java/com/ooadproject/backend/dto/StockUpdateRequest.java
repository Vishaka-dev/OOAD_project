package com.ooadproject.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockUpdateRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Stock level is required")
    @Min(value = 0, message = "Stock level cannot be negative")
    private Integer stockLevel;

    @Min(value = 0, message = "Threshold cannot be negative")
    private Integer lowStockThreshold = 10;
}
