package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.AdminDashboardDTO;
import com.ooadproject.backend.dto.SalesReportDTO;
import com.ooadproject.backend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<AdminDashboardDTO> getDashboardData() {
        AdminDashboardDTO dashboard = reportService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/sales-report")
    public ResponseEntity<List<SalesReportDTO>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<SalesReportDTO> report = reportService.getSalesReport(start, end);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}