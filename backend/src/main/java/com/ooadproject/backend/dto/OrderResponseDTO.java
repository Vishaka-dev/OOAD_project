package com.ooadproject.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ooadproject.backend.entities.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Integer orderId;
    private String customerName;
    private String customerEmail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;
    private Order.OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDate deliveryScheduledDate;
    private String deliveryAddress;
    private String contactNumber;
    private List<OrderItemDTO> orderItems;
}