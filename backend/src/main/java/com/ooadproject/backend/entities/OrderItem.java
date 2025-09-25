package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be positive")
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> personalizationDetails;

    // ---------- Constructors ----------
    public OrderItem() {
    }

    public OrderItem(Integer itemId, Order order, Product product, Integer quantity, BigDecimal price, Map<String, Object> personalizationDetails) {
        this.itemId = itemId;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.personalizationDetails = personalizationDetails;
    }

    // ---------- Getters & Setters ----------
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Map<String, Object> getPersonalizationDetails() {
        return personalizationDetails;
    }

    public void setPersonalizationDetails(Map<String, Object> personalizationDetails) {
        this.personalizationDetails = personalizationDetails;
    }
}