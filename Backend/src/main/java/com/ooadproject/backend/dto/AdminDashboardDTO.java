package com.ooadproject.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Integer lowStockCount;
    private Integer outOfStockCount;
    private List<InventoryDTO> lowStockItems;
    private List<OrderResponseDTO> recentOrders;
}
