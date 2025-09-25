package com.ooadproject.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

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
    @Column(name = "extra_price")
    private Double extraPrice;

    @ColumnDefault("20")
    @Column(name = "max_length")
    private Integer maxLength;

    public enum Color {
        Red, Purple, White, Yello
    }

    // ---------- Constructors ----------
    public PersonalizationOption() {
    }

    public PersonalizationOption(Integer optionId,
                                 Product product,
                                 String usiType,
                                 String massage,
                                 Color color,
                                 Double extraPrice,
                                 Integer maxLength) {
        this.optionId = optionId;
        this.product = product;
        this.usiType = usiType;
        this.massage = massage;
        this.color = color;
        this.extraPrice = extraPrice;
        this.maxLength = maxLength;
    }

    // ---------- Getters & Setters ----------
    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUsiType() {
        return usiType;
    }

    public void setUsiType(String usiType) {
        this.usiType = usiType;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Double getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(Double extraPrice) {
        this.extraPrice = extraPrice;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

}
