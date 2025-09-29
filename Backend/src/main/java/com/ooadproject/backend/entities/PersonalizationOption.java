package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "personalization_options")
public class PersonalizationOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id", nullable = false)
    private Integer optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "usi_type", length = 50)
    private String usiType;

    @Column(name = "massage", length = 100)
    private String massage;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Red'")
    @Column(name = "color")
    private Color color;

    @ColumnDefault("0.00")
    @Column(name = "extra_price", precision = 10, scale = 2)
    private java.math.BigDecimal extraPrice;

    @ColumnDefault("20")
    @Column(name = "max_length")
    private Integer maxLength;

    public enum Color {
        Red, Purple, White, Yello
    }
}
