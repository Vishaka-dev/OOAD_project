package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> personalizationDetails;

    // Constructors
    public CartItem() {
    }

    public CartItem(Integer itemId, Cart cart, Product product, Integer quantity,
                    Map<String, Object> personalizationDetails) {
        this.itemId = itemId;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.personalizationDetails = personalizationDetails;
    }

    // Getters & Setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
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

    public Map<String, Object> getPersonalizationDetails() {
        return personalizationDetails;
    }

    public void setPersonalizationDetails(Map<String, Object> personalizationDetails) {
        this.personalizationDetails = personalizationDetails;
    }
}