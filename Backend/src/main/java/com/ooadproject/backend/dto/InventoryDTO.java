package com.ooadproject.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private Integer productId;
    private String productName;
    private String categoryName;
    private Integer stockLevel;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private String imageUrl;
    private BigDecimal price;
}