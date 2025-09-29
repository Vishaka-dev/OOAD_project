package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesReportDTO {
    private LocalDate date;
    private Long orderCount;
    private BigDecimal totalSales;
    private BigDecimal averageOrderValue;
}
