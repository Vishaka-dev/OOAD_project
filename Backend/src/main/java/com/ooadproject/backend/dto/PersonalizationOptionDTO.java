package com.ooadproject.backend.dto;

import lombok.Data;

@Data
public class PersonalizationOptionDTO {
    private Integer optionId;
    private Integer productId;
    private String usiType;
    private String massage;
    private String color; // enum name
    private Double extraPrice;
    private Integer maxLength;
}


