package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.AdminDashboardStatsDTO;
import com.ooadproject.backend.dto.OrderSummaryDTO;
import com.ooadproject.backend.dto.ProductSalesDTO;
import com.ooadproject.backend.dto.SalesReportDTO;
import com.ooadproject.backend.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        AdminDashboardStatsDTO stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sales-report")
    public ResponseEntity<List<SalesReportDTO>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SalesReportDTO> report = adminDashboardService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductSalesDTO> topProducts = adminDashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<OrderSummaryDTO>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        List<OrderSummaryDTO> recentOrders = adminDashboardService.getRecentOrders(limit);
        return ResponseEntity.ok(recentOrders);
    }
}
