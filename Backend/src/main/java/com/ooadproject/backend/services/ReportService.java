package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.AdminDashboardDTO;
import com.ooadproject.backend.dto.SalesReportDTO;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class ReportService {

    @Autowired
    private AdminOrderService adminOrderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderRepository orderRepository;

    public AdminDashboardDTO getDashboardData() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // Order statistics
        dashboard.setTotalOrders(adminOrderService.getTotalOrderCount());
        dashboard.setPendingOrders(adminOrderService.getOrderCountByStatus(Order.OrderStatus.Pending));
        dashboard.setConfirmedOrders(adminOrderService.getOrderCountByStatus(Order.OrderStatus.Confirmed));
        dashboard.setShippedOrders(adminOrderService.getOrderCountByStatus(Order.OrderStatus.Shipped));
        dashboard.setDeliveredOrders(adminOrderService.getOrderCountByStatus(Order.OrderStatus.Delivered));

        // Revenue statistics
        dashboard.setTotalRevenue(calculateTotalRevenue());
        dashboard.setTodayRevenue(calculateTodayRevenue());

        // Inventory statistics
        dashboard.setLowStockCount(inventoryService.getLowStockItems().size());
        dashboard.setOutOfStockCount(inventoryService.getOutOfStockItems().size());
        dashboard.setLowStockItems(inventoryService.getLowStockItems());

        // Recent orders
        dashboard.setRecentOrders(adminOrderService.getRecentOrders(5));

        return dashboard;
    }

    public List<SalesReportDTO> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<SalesReportDTO> report = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            SalesReportDTO dailyReport = generateDailySalesReport(currentDate);
            report.add(dailyReport);
            currentDate = currentDate.plusDays(1);
        }

        return report;
    }

    private SalesReportDTO generateDailySalesReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Order> dailyOrders = adminOrderService.getOrdersByDateRange(startOfDay, endOfDay);

        SalesReportDTO report = new SalesReportDTO();
        report.setDate(date);
        report.setOrderCount((long) dailyOrders.size());

        BigDecimal dailyRevenue = dailyOrders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.Delivered ||
                        order.getStatus() == Order.OrderStatus.Confirmed)
                .map(order -> BigDecimal.valueOf(order.getTotalPrice())) // ✅ convert to BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setRevenue(dailyRevenue);

        if (!dailyOrders.isEmpty()) {
            report.setAverageOrderValue(dailyRevenue.divide(
                    BigDecimal.valueOf(dailyOrders.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.setAverageOrderValue(BigDecimal.ZERO);
        }

        report.setTopSellingCategory("Category Analysis Available");

        return report;
    }

    private BigDecimal calculateTotalRevenue() {
        return orderRepository.findByStatus(Order.OrderStatus.Delivered).stream()
                .map(order -> BigDecimal.valueOf(order.getTotalPrice())) // ✅ fixed
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        return adminOrderService.getOrdersByDateRange(startOfDay, endOfDay).stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.Delivered ||
                        order.getStatus() == Order.OrderStatus.Confirmed)
                .map(order -> BigDecimal.valueOf(order.getTotalPrice())) // ✅ fixed
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
