package com.ooadproject.backend.entity;


import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Pending'")
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "delivery_scheduled_date")
    private LocalDate deliveryScheduledDate;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<DeliverySlot> deliverySlots = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<Payment> payments = new LinkedHashSet<>();

    public enum Status {
        Pending, Confirmed, Shipped, Delivered, Cancelled
    }
}