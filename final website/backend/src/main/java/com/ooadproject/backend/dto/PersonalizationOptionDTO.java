package com.ooadproject.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PersonalizationOptionDTO {
    private Integer optionId;
    private Integer productId;
    private String usiType;
    private String massage;
    private String color; // enum name
    private BigDecimal extraPrice;
    private Integer maxLength;
}
