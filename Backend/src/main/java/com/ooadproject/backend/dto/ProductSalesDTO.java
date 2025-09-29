package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSalesDTO {
    private Integer productId;
    private String productName;
    private String categoryName;
    private Long quantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
}