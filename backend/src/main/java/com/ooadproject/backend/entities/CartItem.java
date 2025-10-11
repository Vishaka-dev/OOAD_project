package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Denormalized columns for easier SQL reporting (nullable)
    @Column(name = "occasion")
    private String occasion;

    @Column(name = "teddy")
    private String teddy;

    @Column(name = "teddy_type")
    private String teddyType;

    @Column(name = "teddy_color")
    private String teddyColor;

    @Column(name = "flowers_count")
    private Integer flowersCount;

    @Column(name = "flowers_color")
    private String flowersColor;

    @Column(name = "wrapping_paper")
    private String wrappingPaper;

    @Column(name = "soft_toys")
    private String softToys;

    @Column(name = "felt_design")
    private String feltDesign;
}