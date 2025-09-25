package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ColumnDefault("0")
    @Column(name = "stock_level")
    private Integer stockLevel;

    @ColumnDefault("10")
    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    // ---------- Constructors ----------
    public Inventory() {
    }

    public Inventory(Integer productId,
                     Product product,
                     Integer stockLevel,
                     Integer lowStockThreshold) {
        this.productId = productId;
        this.product = product;
        this.stockLevel = stockLevel;
        this.lowStockThreshold = lowStockThreshold;
    }

    // ---------- Getters & Setters ----------
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}