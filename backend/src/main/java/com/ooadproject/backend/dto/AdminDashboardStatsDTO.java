package com.ooadproject.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminDashboardStatsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private Long totalCustomers;
    private Long totalProducts;
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private Long activeProducts;
}