package com.ooadproject.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryDTO {
    private Integer productId;
    private String productName;
    private String categoryName;
    private Integer stockLevel;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private LocalDateTime lastUpdated;
}