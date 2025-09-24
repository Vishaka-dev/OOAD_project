package com.ooadproject.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {
    private LocalDate date;
    private Long orderCount;
    private BigDecimal revenue;
    private BigDecimal averageOrderValue;
    private String topSellingCategory;
}
