package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "stock_level")
    private Integer stockLevel = 0;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 10;

    @OneToOne
    @JoinColumn(name = "product_id")
    @MapsId
    private Product product;
}
