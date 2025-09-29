package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryDTO {
    private Integer orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalPrice;
    private Integer itemCount;
}