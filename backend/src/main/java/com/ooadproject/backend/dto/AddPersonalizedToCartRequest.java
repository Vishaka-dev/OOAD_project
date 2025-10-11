package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AddPersonalizedToCartRequest {
    private Integer productId;
    private Integer quantity;

    // Personalization option fields to persist
    private String usiType;
    private String massage;
    private String color; // enum name in PersonalizationOption.Color
    private BigDecimal extraPrice;
    private Integer maxLength;

    // Additional arbitrary details to store with the cart item (optional)
    private Map<String, Object> additionalDetails;
}
