package com.ooadproject.backend.dto;


import com.ooadproject.backend.entities.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CheckoutRequestDTO {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    // Cart items for guest checkout
    private List<CartItemCheckoutDTO> cartItems;

    // Optional fields for credit card payments
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;

    @Data
    public static class CartItemCheckoutDTO {
        private Integer productId;
        private String productName;
        private java.math.BigDecimal productPrice;
        private Integer quantity;
        private Map<String, Object> personalizationDetails;
        private String customizationId;
    }
}