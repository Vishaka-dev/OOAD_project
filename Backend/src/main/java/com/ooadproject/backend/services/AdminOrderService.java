package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.OrderResponseDTO;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable)
                .map(this::convertToOrderResponseDTO);
    }

    public List<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findAllByOrderByOrderDateDesc(pageable)
                .getContent().stream()
                .map(this::convertToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public Order updateOrderStatus(Integer orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order updated = orderRepository.save(order);

        // Send email notification based on status change
        if (status == Order.OrderStatus.Confirmed && previousStatus != Order.OrderStatus.Confirmed) {
            emailService.sendOrderConfirmation(updated);
        } else if (status != previousStatus) {
            emailService.sendOrderStatusUpdate(updated);
        }

        return updated;
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    public Long getTotalOrderCount() {
        return orderRepository.count();
    }

    public Long getOrderCountByStatus(Order.OrderStatus status) {
        return (long) orderRepository.findByStatus(status).size();
    }

    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTotalPrice(BigDecimal.valueOf(order.getTotalPrice()));
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setContactNumber(order.getContactNumber());
        dto.setDeliveryScheduledDate(order.getDeliveryScheduledDate());

        if (order.getUser() != null) {
            dto.setCustomerName(order.getUser().getUsername());
            dto.setCustomerEmail(order.getUser().getEmail());
        }

        return dto;
    }
}
