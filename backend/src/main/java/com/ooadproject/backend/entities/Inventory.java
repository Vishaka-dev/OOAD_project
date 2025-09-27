package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
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
}