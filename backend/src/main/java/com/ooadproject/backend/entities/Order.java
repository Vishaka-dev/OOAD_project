package com.ooadproject.backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
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

    // ---------- Constructors ----------
    public Order() {
    }

    public Order(Integer orderId, User user, LocalDateTime orderDate, OrderStatus status, BigDecimal totalPrice, LocalDate deliveryScheduledDate, String deliveryAddress, String contactNumber, List<OrderItem> orderItems, List<Payment> payments) {
        this.orderId = orderId;
        this.user = user;
        this.orderDate = orderDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.deliveryScheduledDate = deliveryScheduledDate;
        this.deliveryAddress = deliveryAddress;
        this.contactNumber = contactNumber;
        this.orderItems = orderItems;
        this.payments = payments;
    }

    // ---------- Getters & Setters ----------
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getDeliveryScheduledDate() {
        return deliveryScheduledDate;
    }

    public void setDeliveryScheduledDate(LocalDate deliveryScheduledDate) {
        this.deliveryScheduledDate = deliveryScheduledDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}