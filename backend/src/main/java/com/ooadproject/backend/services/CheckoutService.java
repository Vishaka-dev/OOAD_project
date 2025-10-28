package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CheckoutRequestDTO;
import com.ooadproject.backend.entities.*;
import com.ooadproject.backend.repositories.CartItemRepository;
import com.ooadproject.backend.repositories.CartRepository;
import com.ooadproject.backend.repositories.OrderItemRepository;
import com.ooadproject.backend.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final ProductService productService;
    private final JavaMailSender mailSender;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @Transactional
    public Order checkout(User user, CheckoutRequestDTO request) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.Confirmed); // FIXED: Use OrderStatus enum
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setContactNumber(request.getContactNumber());

        BigDecimal total = BigDecimal.ZERO; // FIXED: Use BigDecimal for consistency
        order = orderRepository.save(order);

        for (CartItem ci : cart.getCartItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());

            // Copy personalization JSON from cart item to order item
            oi.setPersonalizationDetails(ci.getPersonalizationDetails());

            // Calculate price including personalization extra cost
            BigDecimal basePrice = ci.getProduct().getPrice();
            BigDecimal extraCost = BigDecimal.ZERO;

            if (ci.getPersonalizationDetails() != null) {
                // Extract extra cost from personalization details
                Object extraCostObj = ci.getPersonalizationDetails().get("extra_cost");
                if (extraCostObj instanceof BigDecimal) {
                    extraCost = (BigDecimal) extraCostObj;
                } else if (extraCostObj instanceof Number) {
                    extraCost = BigDecimal.valueOf(((Number) extraCostObj).doubleValue());
                }
            }

            // Store unit price including personalization cost
            BigDecimal unitPrice = basePrice.add(extraCost);
            oi.setPrice(unitPrice);

            // Calculate line total for order total
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);

            orderItemRepository.save(oi);
        }

        order.setTotalPrice(total);
        order = orderRepository.save(order);

        // Decrement stock quantities for all products in the order
        System.out.println("🔄 Decrementing stock quantities for order: " + order.getOrderId());
        productService.decrementStockForOrder(cart.getCartItems());

        // FIXED: Handle payment method properly
        Payment.PaymentMethod method = request.getPaymentMethod(); // Direct assignment since it's already enum
        paymentService.createPayment(order, method, total);

        // Send confirmation email (non-blocking - don't fail checkout if email fails)
        try {
            System.out.println("📧 Attempting to send order confirmation email...");
            sendConfirmationEmail(request, order); // FIXED: Pass CheckoutRequestDTO
            System.out.println("✅ Order confirmation email sent successfully");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send order confirmation email: " + e.getMessage());
            System.err.println("⚠️ Order was still created successfully - email failure is non-critical");
            // Log but don't throw - email failure should not prevent checkout
        }

        // clear cart
        cart.getCartItems().forEach(cartItemRepository::delete);

        return order;
    }

    // FIXED: Parameter type should be CheckoutRequestDTO
    private void sendConfirmationEmail(CheckoutRequestDTO request, Order order) {
        System.out.println("📧 Preparing to send confirmation email for order: " + order.getOrderId());

        String subject = "Order #" + order.getOrderId() + " confirmed";
        String body = "Order confirmed. Deliver within 5 days.\n" +
                "Order ID: " + order.getOrderId() + "\n" +
                "Total: $" + order.getTotalPrice() + "\n" +
                "Delivery Address: " + order.getDeliveryAddress();

        // Send to admin
        System.out.println("📧 Sending email to admin: " + adminEmail);
        SimpleMailMessage adminMsg = new SimpleMailMessage();
        adminMsg.setFrom(order.getUser().getEmail()); // Set from address
        adminMsg.setTo(adminEmail);
        adminMsg.setSubject(subject);
        adminMsg.setText(body);
        mailSender.send(adminMsg);
        System.out.println("✅ Admin email sent successfully");

        // Send to customer - use user's email from the User entity
        String customerEmail = order.getUser().getEmail();
        System.out.println("📧 Sending email to customer: " + customerEmail);
        SimpleMailMessage custMsg = new SimpleMailMessage();
        custMsg.setFrom(order.getUser().getEmail()); // Set from address
        custMsg.setTo(customerEmail);
        custMsg.setSubject(subject);
        custMsg.setText(body);
        mailSender.send(custMsg);
        System.out.println("✅ Customer email sent successfully");
    }
}