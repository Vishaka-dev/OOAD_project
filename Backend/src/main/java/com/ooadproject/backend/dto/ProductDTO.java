package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stockQuantity;
    private Boolean inStock;
}
