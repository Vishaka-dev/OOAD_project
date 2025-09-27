package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderItemDTO {
    private Integer itemId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private Map<String, Object> personalizationDetails;
    private BigDecimal itemTotal;
}