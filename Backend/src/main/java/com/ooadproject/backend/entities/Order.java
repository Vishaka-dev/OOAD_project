package com.ooadproject.backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.Pending;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.00", message = "Total price must be positive")
    private BigDecimal totalPrice;

    private LocalDate deliveryScheduledDate;

    @Column(nullable = false)
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    public enum OrderStatus {
        Pending, Confirmed, Shipped, Delivered, Cancelled
    }
}