
package com.ooadproject.backend.service;

import com.ooadproject.backend.dto.RecentOrderDTO;
import com.ooadproject.backend.entity.Order;
import com.ooadproject.backend.entity.Order.OrderStatus;
import com.ooadproject.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailService emailService;

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<RecentOrderDTO> getRecentOrders(int limit) {
        return orderRepository.findTop10ByOrderByOrderDateDesc().stream()
                .limit(limit)
                .map(this::convertToRecentOrderDTO)
                .collect(Collectors.toList());
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order updated = orderRepository.save(order);

        // Send email notification based on status change
        if (status == OrderStatus.CONFIRMED && previousStatus == OrderStatus.PENDING) {
            emailService.sendOrderConfirmationEmail(updated);
        } else if (status == OrderStatus.SHIPPED) {
            emailService.sendOrderShippedEmail(updated);
        } else if (status == OrderStatus.DELIVERED) {
            emailService.sendOrderDeliveredEmail(updated);
        }

        return updated;
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    public Long getTotalOrderCount() {
        return orderRepository.count();
    }

    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    private RecentOrderDTO convertToRecentOrderDTO(Order order) {
        RecentOrderDTO dto = new RecentOrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryAddress(order.getDeliveryAddress());

        // Assuming you have user relationship
        // dto.setCustomerName(order.getUser().getUsername());

        return dto;
    }
}
