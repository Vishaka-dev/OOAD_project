package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CartItemDTO {
    private Integer itemId;
    private Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private String imageUrl;
    private Integer quantity;
    private Map<String, Object> personalizationDetails;
    private BigDecimal extraPrice = BigDecimal.ZERO;
    private BigDecimal itemTotal;
}