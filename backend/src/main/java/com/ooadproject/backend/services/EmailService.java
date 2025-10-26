package com.ooadproject.backend.services;

import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SendGridEmailService sendGridEmailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.sendgrid.enabled:true}")
    private boolean sendGridEnabled;

    public void sendOrderConfirmation(Order order) {
        // Try SendGrid first (works on Railway)
        if (sendGridEnabled) {
            log.info("Using SendGrid for order confirmation email");
            sendGridEmailService.sendOrderConfirmation(order);
            return;
        }

        // Fallback to SMTP
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping order confirmation email for order: {}", order.getOrderId());
            return;
        }

        try {
            log.info("Attempting to send order confirmation email for order: {} to: {}",
                    order.getOrderId(), order.getUser().getEmail());
            log.info("Email configuration - From: {}, Host: {}, Port: {}",
                    fromEmail, "smtp.gmail.com", "587");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
                log.info("Set from address: {} with name: {}", fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
                log.warn("Failed to set from name, using email only: {}", fromEmail);
            }
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("🎉 Order Confirmation - Order #" + order.getOrderId());
            helper.setText(buildOrderConfirmationHtml(order), true);

            log.info("Sending email via mailSender...");
            mailSender.send(message);
            log.info("✅ Order confirmation email sent successfully for order: {}", order.getOrderId());

            // Also send notification to admin
            sendAdminOrderNotification(order);
        } catch (MessagingException e) {
            log.error("❌ Failed to send order confirmation email for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
            log.error("Full stack trace:", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
            log.error("Full stack trace:", e);
        }
    }

    public void sendOrderStatusUpdate(Order order) {
        // Try SendGrid first (works on Railway)
        if (sendGridEnabled) {
            log.info("Using SendGrid for order status update email");
            sendGridEmailService.sendOrderStatusUpdate(order);
            return;
        }

        // Fallback to SMTP
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping order status update email for order: {}", order.getOrderId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("📦 Order Status Update - Order #" + order.getOrderId());
            helper.setText(buildStatusUpdateHtml(order), true);

            mailSender.send(message);
            log.info("Order status update email sent for order: {}", order.getOrderId());
        } catch (MessagingException e) {
            log.error("Failed to send order status update email for order: {}", order.getOrderId(), e);
        }
    }

    private void sendAdminOrderNotification(Order order) {
        if (!emailEnabled) {
            return;
        }

        try {
            log.info("Attempting to send admin notification email for order: {} to: {}",
                    order.getOrderId(), adminEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(adminEmail);
            helper.setSubject("🛒 New Order Received - Order #" + order.getOrderId());
            helper.setText(buildAdminNotificationHtml(order), true);

            mailSender.send(message);
            log.info("✅ Admin notification email sent successfully for order: {}", order.getOrderId());
        } catch (MessagingException e) {
            log.error("❌ Failed to send admin notification email for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
            log.error("Full stack trace:", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending admin email for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
            log.error("Full stack trace:", e);
        }
    }

    public void sendOrderCancellation(Order order) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping order cancellation email for order: {}", order.getOrderId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("❌ Order Cancelled - Order #" + order.getOrderId());
            helper.setText(buildOrderCancellationHtml(order), true);

            mailSender.send(message);
            log.info("Order cancellation email sent for order: {}", order.getOrderId());
        } catch (MessagingException e) {
            log.error("Failed to send order cancellation email for order: {}", order.getOrderId(), e);
        }
    }

    public void sendDeliveryNotification(Order order) {
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping delivery notification email for order: {}",
                    order.getOrderId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("🚚 Your Order is Out for Delivery - Order #" + order.getOrderId());
            helper.setText(buildDeliveryNotificationHtml(order), true);

            mailSender.send(message);
            log.info("Delivery notification email sent for order: {}", order.getOrderId());
        } catch (MessagingException e) {
            log.error("Failed to send delivery notification email for order: {}", order.getOrderId(), e);
        }
    }

    public void sendOrderSummary(Order order) {
        // Try SendGrid first (works on Railway)
        if (sendGridEnabled) {
            log.info("Using SendGrid for admin order notification");
            sendGridEmailService.sendAdminOrderNotification(order);
            return;
        }

        // Fallback to SMTP
        if (!emailEnabled) {
            log.info("Email service is disabled. Skipping order summary email for order: {}", order.getOrderId());
            return;
        }

        try {
            // Send to customer
            MimeMessage customerMessage = mailSender.createMimeMessage();
            MimeMessageHelper customerHelper = new MimeMessageHelper(customerMessage, true, "UTF-8");

            try {
                customerHelper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                customerHelper.setFrom(fromEmail);
            }
            customerHelper.setTo(order.getUser().getEmail());
            customerHelper.setSubject("📋 Order Summary - Order #" + order.getOrderId());
            customerHelper.setText(buildOrderSummaryHtml(order, false), true);

            mailSender.send(customerMessage);
            log.info("Order summary email sent to customer for order: {}", order.getOrderId());

            // Send to admin
            MimeMessage adminMessage = mailSender.createMimeMessage();
            MimeMessageHelper adminHelper = new MimeMessageHelper(adminMessage, true, "UTF-8");

            try {
                adminHelper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                adminHelper.setFrom(fromEmail);
            }
            adminHelper.setTo(adminEmail);
            adminHelper.setSubject("📊 Order Summary Report - Order #" + order.getOrderId());
            adminHelper.setText(buildOrderSummaryHtml(order, true), true);

            mailSender.send(adminMessage);
            log.info("Order summary email sent to admin for order: {}", order.getOrderId());

        } catch (MessagingException e) {
            log.error("Failed to send order summary email for order: {}", order.getOrderId(), e);
        }
    }

    private String buildOrderConfirmationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #f8f9fa; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".order-details { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".item { border-bottom: 1px solid #eee; padding: 10px 0; }");
        html.append(".total { font-weight: bold; font-size: 18px; color: #28a745; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>🎉 Order Confirmed!</h1>");
        html.append("<p>Thank you for your order, ").append(order.getUser().getUsername()).append("!</p>");
        html.append("</div>");

        html.append("<div class='order-details'>");
        html.append("<h2>Order Details</h2>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ")
                .append(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        html.append("<p><strong>Contact Number:</strong> ").append(order.getContactNumber()).append("</p>");
        html.append("<p><strong>Expected Delivery:</strong> ").append(order.getDeliveryScheduledDate()).append("</p>");

        html.append("<h3>Order Items</h3>");
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                html.append("<div class='item'>");
                html.append("<p><strong>").append(item.getProduct().getName()).append("</strong></p>");
                html.append("<p>Quantity: ").append(item.getQuantity()).append("</p>");
                html.append("<p>Base Price: $").append(item.getPrice()).append(" each</p>");

                // Calculate and display extra pricing for personalizations
                BigDecimal extraPrice = calculateExtraPrice(item.getPersonalizationDetails());
                if (extraPrice.compareTo(BigDecimal.ZERO) > 0) {
                    html.append("<p>Personalization Extra: $").append(extraPrice).append(" each</p>");
                    html.append("<p>Total per item: $").append(item.getPrice().add(extraPrice)).append("</p>");
                }

                BigDecimal itemTotal = (item.getPrice().add(extraPrice))
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                html.append("<p><strong>Subtotal: $").append(itemTotal).append("</strong></p>");

                // Display detailed personalization information
                if (item.getPersonalizationDetails() != null && !item.getPersonalizationDetails().isEmpty()) {
                    html.append(
                            "<div style='background: #f8f9fa; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
                    html.append("<p><strong>🎨 Personalization Details:</strong></p>");
                    html.append("<ul style='margin: 5px 0; padding-left: 20px;'>");

                    item.getPersonalizationDetails().forEach((key, value) -> {
                        if (value != null && !value.toString().trim().isEmpty()) {
                            String displayKey = formatPersonalizationKey(key);
                            html.append("<li><strong>").append(displayKey).append(":</strong> ").append(value)
                                    .append("</li>");
                        }
                    });

                    html.append("</ul>");
                    html.append("</div>");
                }
                html.append("</div>");
            }
        }

        html.append("<div class='total'>");
        html.append("<p>Total Amount: $").append(order.getTotalPrice()).append("</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Your order will be delivered within 5 days.</p>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("<p><strong>Gift Shop Team</strong></p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildStatusUpdateHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #f8f9fa; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".status { background: #e3f2fd; border: 1px solid #2196f3; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>📦 Order Status Update</h1>");
        html.append("</div>");

        html.append("<div class='status'>");
        html.append("<h2>Hello ").append(order.getUser().getUsername()).append("!</h2>");
        html.append("<p>Your order status has been updated.</p>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Current Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");

        if (order.getStatus() == Order.OrderStatus.Delivered) {
            html.append(
                    "<p><strong>🎉 Great news!</strong> Your order has been delivered! We hope you enjoy your purchase.</p>");
        } else if (order.getStatus() == Order.OrderStatus.Shipped) {
            html.append("<p><strong>🚚 Your order is on its way!</strong> Expected delivery: ")
                    .append(order.getDeliveryScheduledDate()).append("</p>");
        } else if (order.getStatus() == Order.OrderStatus.Confirmed) {
            html.append("<p><strong>✅ Your order has been confirmed</strong> and is being prepared for shipment.</p>");
        } else if (order.getStatus() == Order.OrderStatus.Cancelled) {
            html.append(
                    "<p><strong>❌ Your order has been cancelled.</strong> If you have any questions, please contact our support team.</p>");
        }

        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("<p><strong>Gift Shop Team</strong></p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildAdminNotificationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(
                ".header { background: #fff3cd; border: 1px solid #ffeaa7; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".order-details { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".item { border-bottom: 1px solid #eee; padding: 10px 0; }");
        html.append(".total { font-weight: bold; font-size: 18px; color: #28a745; }");
        html.append(
                ".urgent { background: #f8d7da; border: 1px solid #f5c6cb; padding: 10px; border-radius: 5px; margin: 10px 0; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>🛒 New Order Received</h1>");
        html.append("<p>Order #").append(order.getOrderId()).append(" from ").append(order.getUser().getUsername())
                .append("</p>");
        html.append("</div>");

        html.append("<div class='order-details'>");
        html.append("<h2>Order Information</h2>");
        html.append("<p><strong>Customer:</strong> ").append(order.getUser().getUsername()).append(" (")
                .append(order.getUser().getEmail()).append(")</p>");
        html.append("<p><strong>Order Date:</strong> ")
                .append(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        html.append("<p><strong>Contact Number:</strong> ").append(order.getContactNumber()).append("</p>");
        html.append("<p><strong>Expected Delivery:</strong> ").append(order.getDeliveryScheduledDate()).append("</p>");

        html.append("<h3>Order Items</h3>");
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                html.append("<div class='item'>");
                html.append("<p><strong>").append(item.getProduct().getName()).append("</strong></p>");
                html.append("<p>Quantity: ").append(item.getQuantity()).append("</p>");
                html.append("<p>Base Price: $").append(item.getPrice()).append(" each</p>");

                // Calculate and display extra pricing for personalizations
                BigDecimal extraPrice = calculateExtraPrice(item.getPersonalizationDetails());
                if (extraPrice.compareTo(BigDecimal.ZERO) > 0) {
                    html.append("<p>Personalization Extra: $").append(extraPrice).append(" each</p>");
                    html.append("<p>Total per item: $").append(item.getPrice().add(extraPrice)).append("</p>");
                }

                BigDecimal itemTotal = (item.getPrice().add(extraPrice))
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                html.append("<p><strong>Subtotal: $").append(itemTotal).append("</strong></p>");

                // Display detailed personalization information for admin
                if (item.getPersonalizationDetails() != null && !item.getPersonalizationDetails().isEmpty()) {
                    html.append("<div class='urgent'>");
                    html.append("<p><strong>🎨 Personalization Details (Action Required):</strong></p>");
                    html.append("<ul style='margin: 5px 0; padding-left: 20px;'>");

                    item.getPersonalizationDetails().forEach((key, value) -> {
                        if (value != null && !value.toString().trim().isEmpty()) {
                            String displayKey = formatPersonalizationKey(key);
                            html.append("<li><strong>").append(displayKey).append(":</strong> ").append(value)
                                    .append("</li>");
                        }
                    });

                    html.append("</ul>");
                    html.append(
                            "<p><em>Please ensure all personalization requirements are met before shipping.</em></p>");
                    html.append("</div>");
                }
                html.append("</div>");
            }
        }

        html.append("<div class='total'>");
        html.append("<p>Total Amount: $").append(order.getTotalPrice()).append("</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildOrderCancellationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(
                ".header { background: #f8d7da; border: 1px solid #f5c6cb; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".order-details { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>❌ Order Cancelled</h1>");
        html.append("<p>We're sorry to inform you that your order has been cancelled.</p>");
        html.append("</div>");

        html.append("<div class='order-details'>");
        html.append("<h2>Order Details</h2>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ")
                .append(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
        html.append("<p><strong>Total Amount:</strong> $").append(order.getTotalPrice()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>If you have any questions about this cancellation, please contact our support team.</p>");
        html.append("<p>Thank you for your understanding.</p>");
        html.append("<p><strong>Gift Shop Team</strong></p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildDeliveryNotificationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(
                ".header { background: #d4edda; border: 1px solid #c3e6cb; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".order-details { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>🚚 Out for Delivery!</h1>");
        html.append("<p>Great news! Your order is on its way to you.</p>");
        html.append("</div>");

        html.append("<div class='order-details'>");
        html.append("<h2>Delivery Information</h2>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        html.append("<p><strong>Contact Number:</strong> ").append(order.getContactNumber()).append("</p>");
        html.append("<p><strong>Expected Delivery:</strong> ").append(order.getDeliveryScheduledDate()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Please ensure someone is available at the delivery address to receive your order.</p>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("<p><strong>Gift Shop Team</strong></p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private String buildOrderSummaryHtml(Order order, boolean isAdmin) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(
                ".header { background: #e3f2fd; border: 1px solid #2196f3; padding: 20px; text-align: center; border-radius: 8px; }");
        html.append(
                ".order-details { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }");
        html.append(".item { border-bottom: 1px solid #eee; padding: 10px 0; }");
        html.append(".total { font-weight: bold; font-size: 18px; color: #28a745; }");
        html.append(".footer { text-align: center; margin-top: 30px; color: #666; }");
        html.append(
                ".urgent { background: #f8d7da; border: 1px solid #f5c6cb; padding: 10px; border-radius: 5px; margin: 10px 0; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");

        if (isAdmin) {
            html.append("<div class='header'>");
            html.append("<h1>📊 Order Summary Report</h1>");
            html.append("<p>Order #").append(order.getOrderId()).append(" from ").append(order.getUser().getUsername())
                    .append("</p>");
            html.append("</div>");
        } else {
            html.append("<div class='header'>");
            html.append("<h1>📋 Order Summary</h1>");
            html.append("<p>Thank you for your order, ").append(order.getUser().getUsername()).append("!</p>");
            html.append("</div>");
        }

        html.append("<div class='order-details'>");
        html.append("<h2>Order Information</h2>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ")
                .append(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(order.getStatus()).append("</p>");

        if (isAdmin) {
            html.append("<p><strong>Customer:</strong> ").append(order.getUser().getUsername()).append(" (")
                    .append(order.getUser().getEmail()).append(")</p>");
        }

        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        html.append("<p><strong>Contact Number:</strong> ").append(order.getContactNumber()).append("</p>");
        html.append("<p><strong>Expected Delivery:</strong> ").append(order.getDeliveryScheduledDate()).append("</p>");

        html.append("<h3>Order Items</h3>");
        BigDecimal totalPersonalizationCost = BigDecimal.ZERO;

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                html.append("<div class='item'>");
                html.append("<p><strong>").append(item.getProduct().getName()).append("</strong></p>");
                html.append("<p>Quantity: ").append(item.getQuantity()).append("</p>");
                html.append("<p>Base Price: $").append(item.getPrice()).append(" each</p>");

                // Calculate and display extra pricing for personalizations
                BigDecimal extraPrice = calculateExtraPrice(item.getPersonalizationDetails());
                if (extraPrice.compareTo(BigDecimal.ZERO) > 0) {
                    html.append("<p>Personalization Extra: $").append(extraPrice).append(" each</p>");
                    html.append("<p>Total per item: $").append(item.getPrice().add(extraPrice)).append("</p>");
                    totalPersonalizationCost = totalPersonalizationCost
                            .add(extraPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                BigDecimal itemTotal = (item.getPrice().add(extraPrice))
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                html.append("<p><strong>Subtotal: $").append(itemTotal).append("</strong></p>");

                // Display detailed personalization information
                if (item.getPersonalizationDetails() != null && !item.getPersonalizationDetails().isEmpty()) {
                    if (isAdmin) {
                        html.append("<div class='urgent'>");
                        html.append("<p><strong>🎨 Personalization Details (Action Required):</strong></p>");
                    } else {
                        html.append(
                                "<div style='background: #f8f9fa; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
                        html.append("<p><strong>🎨 Personalization Details:</strong></p>");
                    }

                    html.append("<ul style='margin: 5px 0; padding-left: 20px;'>");

                    item.getPersonalizationDetails().forEach((key, value) -> {
                        if (value != null && !value.toString().trim().isEmpty()) {
                            String displayKey = formatPersonalizationKey(key);
                            html.append("<li><strong>").append(displayKey).append(":</strong> ").append(value)
                                    .append("</li>");
                        }
                    });

                    html.append("</ul>");
                    if (isAdmin) {
                        html.append(
                                "<p><em>Please ensure all personalization requirements are met before shipping.</em></p>");
                    }
                    html.append("</div>");
                }
                html.append("</div>");
            }
        }

        html.append("<div class='total'>");
        html.append("<p>Base Items Total: $").append(order.getTotalPrice().subtract(totalPersonalizationCost))
                .append("</p>");
        if (totalPersonalizationCost.compareTo(BigDecimal.ZERO) > 0) {
            html.append("<p>Personalization Total: $").append(totalPersonalizationCost).append("</p>");
        }
        html.append("<p><strong>Grand Total: $").append(order.getTotalPrice()).append("</strong></p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='footer'>");
        if (isAdmin) {
            html.append("<p>This order requires attention for personalization details.</p>");
            html.append("<p>Please process and ship according to customer specifications.</p>");
        } else {
            html.append("<p>Your order will be delivered within 5 days.</p>");
            html.append("<p>Thank you for shopping with us!</p>");
        }
        html.append("<p><strong>Gift Shop Team</strong></p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private BigDecimal calculateExtraPrice(java.util.Map<String, Object> personalizationDetails) {
        if (personalizationDetails == null || personalizationDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal extraPrice = BigDecimal.ZERO;

        // Occasion pricing
        String occasion = (String) personalizationDetails.get("occasion");
        if (occasion != null) {
            switch (occasion) {
                case "Graduation" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
                case "Birthday" -> extraPrice = extraPrice.add(BigDecimal.valueOf(3));
                case "Valentine" -> extraPrice = extraPrice.add(BigDecimal.valueOf(8));
                case "Mini" -> extraPrice = extraPrice.add(BigDecimal.valueOf(2));
            }
        }

        // Flowers count pricing
        String flowersCount = (String) personalizationDetails.get("flowersCount");
        if (flowersCount != null) {
            try {
                int count = Integer.parseInt(flowersCount);
                extraPrice = extraPrice.add(BigDecimal.valueOf(count));
            } catch (NumberFormatException e) {
                switch (flowersCount) {
                    case "3" -> extraPrice = extraPrice.add(BigDecimal.valueOf(3));
                    case "5" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
                    case "7" -> extraPrice = extraPrice.add(BigDecimal.valueOf(7));
                    case "9" -> extraPrice = extraPrice.add(BigDecimal.valueOf(9));
                    case "12" -> extraPrice = extraPrice.add(BigDecimal.valueOf(12));
                }
            }
        }

        // Teddy pricing
        String teddy = (String) personalizationDetails.get("teddy");
        if ("With".equals(teddy)) {
            extraPrice = extraPrice.add(BigDecimal.valueOf(15));
        }

        String teddyType = (String) personalizationDetails.get("teddyType");
        if (teddyType != null) {
            switch (teddyType) {
                case "handmade" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
                case "fluffy" -> extraPrice = extraPrice.add(BigDecimal.valueOf(10));
            }
        }

        // Wrapping paper pricing
        String wrappingPaper = (String) personalizationDetails.get("wrappingPaper");
        if (wrappingPaper != null) {
            switch (wrappingPaper) {
                case "Premium" -> extraPrice = extraPrice.add(BigDecimal.valueOf(3));
                case "Gift Box" -> extraPrice = extraPrice.add(BigDecimal.valueOf(5));
            }
        }

        // Soft toys pricing
        String softToys = (String) personalizationDetails.get("softToys");
        if ("Yes".equals(softToys)) {
            extraPrice = extraPrice.add(BigDecimal.valueOf(8));
        }

        // Custom felt design pricing
        String feltDesign = (String) personalizationDetails.get("feltDesign");
        if (feltDesign != null && !feltDesign.trim().isEmpty()) {
            extraPrice = extraPrice.add(BigDecimal.valueOf(5));
        }

        return extraPrice;
    }

    private String formatPersonalizationKey(String key) {
        switch (key) {
            case "occasion" -> {
                return "Occasion";
            }
            case "flowersCount" -> {
                return "Flower Count";
            }
            case "flowersColor" -> {
                return "Flower Color";
            }
            case "wrappingPaper" -> {
                return "Wrapping Paper";
            }
            case "teddy" -> {
                return "Include Teddy";
            }
            case "teddyType" -> {
                return "Teddy Type";
            }
            case "teddyColor" -> {
                return "Teddy Color";
            }
            case "feltDesign" -> {
                return "Custom Felt Design";
            }
            case "softToys" -> {
                return "Soft Toys";
            }
            case "usiType" -> {
                return "USI Type";
            }
            case "massage" -> {
                return "Message";
            }
            case "color" -> {
                return "Color";
            }
            case "maxLength" -> {
                return "Max Length";
            }
            default -> {
                return key.substring(0, 1).toUpperCase() + key.substring(1).replaceAll("([A-Z])", " $1");
            }
        }
    }
}
