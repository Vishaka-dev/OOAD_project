package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CheckoutRequestDTO;
import com.ooadproject.backend.entities.*;
import com.ooadproject.backend.repositories.*;
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
    private final JavaMailSender mailSender;

    @Value("${app.mail.admin:admin@example.com}")
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

            // FIXED: Use BigDecimal for price calculations
            BigDecimal lineTotal = ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            oi.setPrice(ci.getProduct().getPrice()); // Store unit price, not line total
            oi.setPersonalizationDetails(ci.getPersonalizationDetails());
            total = total.add(lineTotal);
            orderItemRepository.save(oi);
        }

        order.setTotalPrice(total);
        order = orderRepository.save(order);

        // FIXED: Handle payment method properly
        Payment.PaymentMethod method = request.getPaymentMethod(); // Direct assignment since it's already enum
        paymentService.createPayment(order, method, total);

        sendConfirmationEmail(request, order); // FIXED: Pass CheckoutRequestDTO

        // clear cart
        cart.getCartItems().forEach(cartItemRepository::delete);

        return order;
    }

    // FIXED: Parameter type should be CheckoutRequestDTO
    private void sendConfirmationEmail(CheckoutRequestDTO request, Order order) {
        String subject = "Order #" + order.getOrderId() + " confirmed";
        String body = "Order confirmed. Deliver within 5 days.\n" +
                "Order ID: " + order.getOrderId() + "\n" +
                "Total: $" + order.getTotalPrice() + "\n" +
                "Delivery Address: " + order.getDeliveryAddress();

        // Send to admin
        SimpleMailMessage adminMsg = new SimpleMailMessage();
        adminMsg.setTo(adminEmail);
        adminMsg.setSubject(subject);
        adminMsg.setText(body);
        mailSender.send(adminMsg);

        // Send to customer - use user's email from the User entity
        String customerEmail = order.getUser().getEmail();
        SimpleMailMessage custMsg = new SimpleMailMessage();
        custMsg.setTo(customerEmail);
        custMsg.setSubject(subject);
        custMsg.setText(body);
        mailSender.send(custMsg);
    }
}