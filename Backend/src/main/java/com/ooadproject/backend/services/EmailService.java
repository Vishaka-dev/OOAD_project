package com.ooadproject.backend.services;

import com.ooadproject.backend.entities.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Order Confirmation - Order #" + order.getOrderId());
            message.setText(buildOrderConfirmationText(order));

            mailSender.send(message);
            log.info("Order confirmation email sent for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", order.getOrderId(), e);
        }
    }

    public void sendOrderStatusUpdate(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getUser().getEmail());
            message.setSubject("Order Status Update - Order #" + order.getOrderId());
            message.setText(buildStatusUpdateText(order));

            mailSender.send(message);
            log.info("Order status update email sent for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order status update email for order: {}", order.getOrderId(), e);
        }
    }

    private String buildOrderConfirmationText(Order order) {
        StringBuilder text = new StringBuilder();
        text.append("Dear ").append(order.getUser().getUsername()).append(",\n\n");
        text.append("Thank you for your order! Your order has been confirmed.\n\n");
        text.append("Order Details:\n");
        text.append("Order ID: ").append(order.getOrderId()).append("\n");
        text.append("Total Amount: $").append(order.getTotalPrice()).append("\n");
        text.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n");
        text.append("Expected Delivery: ").append(order.getDeliveryScheduledDate()).append("\n\n");
        text.append("Your order will be delivered within 5 days.\n\n");
        text.append("Thank you for shopping with us!\n");
        text.append("Gift Shop Team");
        return text.toString();
    }

    private String buildStatusUpdateText(Order order) {
        StringBuilder text = new StringBuilder();
        text.append("Dear ").append(order.getUser().getUsername()).append(",\n\n");
        text.append("Your order status has been updated.\n\n");
        text.append("Order ID: ").append(order.getOrderId()).append("\n");
        text.append("Current Status: ").append(order.getStatus()).append("\n");
        text.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n");

        if (order.getStatus() == Order.OrderStatus.Delivered) {
            text.append("\nYour order has been delivered! We hope you enjoy your purchase.\n");
        } else if (order.getStatus() == Order.OrderStatus.Shipped) {
            text.append("\nYour order is on its way! Expected delivery: ").append(order.getDeliveryScheduledDate()).append("\n");
        }

        text.append("\nThank you for shopping with us!\n");
        text.append("Gift Shop Team");
        return text.toString();
    }
}
