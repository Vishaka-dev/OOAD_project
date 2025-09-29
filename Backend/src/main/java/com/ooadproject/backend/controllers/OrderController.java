package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.OrderResponseDTO;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/user")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            @AuthenticationPrincipal User user) {
        List<OrderResponseDTO> orders = orderService.getUserOrders(user);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal User user) {
        OrderResponseDTO order = orderService.getOrderById(orderId, user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/all")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/admin/{orderId}/status")
    // @PreAuthorize("hasRole('ADMIN')") // Temporarily disabled for testing
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        Order.OrderStatus status = Order.OrderStatus.valueOf(request.getStatus());
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // DTO for status update request
    public static class OrderStatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}