package com.ooadproject.backend.services;

import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.OrderItem;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class SendGridEmailService {

    @Value("${app.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from-email:}")
    private String fromEmail;

    @Value("${app.sendgrid.from-name:SendGrid Service}")
    private String fromName;

    @Value("${app.sendgrid.enabled:true}")
    private boolean sendGridEnabled;

    @Value("${app.mail.admin:admin@example.com}")
    private String adminEmail;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @PostConstruct
    public void init() {
        log.info("=================================");
        log.info("📧 SendGrid Email Service Initialized");
        log.info("Enabled: {}", sendGridEnabled);
        log.info("From Email: {}", fromEmail);
        log.info("From Name: {}", fromName);
        log.info("Admin Email: {}", adminEmail);
        log.info("API Key Present: {}", (sendGridApiKey != null && !sendGridApiKey.trim().isEmpty()));
        if (sendGridApiKey != null && !sendGridApiKey.trim().isEmpty()) {
            log.info("API Key Preview: {}...{}",
                    sendGridApiKey.substring(0, Math.min(10, sendGridApiKey.length())),
                    sendGridApiKey.length() > 10 ? sendGridApiKey.substring(sendGridApiKey.length() - 5) : "");
        }
        log.info("=================================");
    }

    public void sendOrderConfirmation(Order order) {
        log.info("🔍 SendGrid status check - Enabled: {}, API Key exists: {}, From Email: {}",
                sendGridEnabled, (sendGridApiKey != null && !sendGridApiKey.isEmpty()), fromEmail);

        if (!sendGridEnabled) {
            log.info("📧 SendGrid is disabled. Skipping order confirmation email for order: {}", order.getOrderId());
            return;
        }

        if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
            log.error("❌ SendGrid API key is missing or empty! Check SENDGRID_API_KEY environment variable.");
            return;
        }

        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.error("❌ SendGrid from email is missing! Check SENDGRID_FROM_EMAIL environment variable.");
            return;
        }

        try {
            log.info("📧 Sending order confirmation via SendGrid for order: {} to: {}",
                    order.getOrderId(), order.getUser().getEmail());

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(order.getUser().getEmail(), order.getUser().getUsername());
            String subject = "Order Confirmation - Order #" + order.getOrderId();

            String htmlContent = buildOrderConfirmationHtml(order);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Order confirmation email sent successfully via SendGrid for order: {}", order.getOrderId());
            } else {
                log.error("❌ SendGrid API returned status code: {} for order: {}",
                        response.getStatusCode(), order.getOrderId());
                log.error("Response body: {}", response.getBody());
            }

        } catch (IOException e) {
            log.error("❌ Failed to send order confirmation email via SendGrid for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
            log.error("Full stack trace:", e);
        }
    }

    public void sendAdminOrderNotification(Order order) {
        if (!sendGridEnabled) {
            log.info("📧 SendGrid is disabled. Skipping admin notification for order: {}", order.getOrderId());
            return;
        }

        try {
            log.info("📧 Sending admin notification via SendGrid for order: {} to: {}",
                    order.getOrderId(), adminEmail);

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(adminEmail, "Admin");
            String subject = "New Order Received - Order #" + order.getOrderId();

            String htmlContent = buildAdminNotificationHtml(order);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Admin notification email sent successfully via SendGrid for order: {}", order.getOrderId());
            } else {
                log.error("❌ SendGrid API returned status code: {} for order: {}",
                        response.getStatusCode(), order.getOrderId());
            }

        } catch (IOException e) {
            log.error("❌ Failed to send admin notification via SendGrid for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
        }
    }

    public void sendOrderStatusUpdate(Order order) {
        if (!sendGridEnabled) {
            log.info("📧 SendGrid is disabled. Skipping status update email for order: {}", order.getOrderId());
            return;
        }

        try {
            log.info("📧 Sending order status update via SendGrid for order: {} to: {}",
                    order.getOrderId(), order.getUser().getEmail());

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(order.getUser().getEmail(), order.getUser().getUsername());
            String subject = "Order Status Update - Order #" + order.getOrderId();

            String htmlContent = buildStatusUpdateHtml(order);
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Order status update email sent successfully via SendGrid for order: {}",
                        order.getOrderId());
            } else {
                log.error("❌ SendGrid API returned status code: {} for order: {}",
                        response.getStatusCode(), order.getOrderId());
            }

        } catch (IOException e) {
            log.error("❌ Failed to send status update email via SendGrid for order: {}", order.getOrderId());
            log.error("Error details: {}", e.getMessage());
        }
    }

    private String buildOrderConfirmationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append(
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        html.append(
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>");

        html.append(
                "<h2 style='color: #ec4899; border-bottom: 2px solid #ec4899; padding-bottom: 10px;'>Order Confirmation</h2>");
        html.append("<p>Dear ").append(order.getUser().getUsername()).append(",</p>");
        html.append("<p>Thank you for your order! We're excited to prepare your special gifts.</p>");

        html.append("<div style='background-color: #f9fafb; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0; color: #ec4899;'>Order Details</h3>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ").append(order.getOrderDate().format(dateFormatter))
                .append("</p>");
        html.append("<p><strong>Status:</strong> <span style='color: #10b981; font-weight: bold;'>")
                .append(order.getStatus()).append("</span></p>");
        html.append("</div>");

        html.append("<h3 style='color: #ec4899;'>Items Ordered</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px;'>");
        html.append("<thead><tr style='background-color: #fce7f3;'>");
        html.append("<th style='padding: 10px; text-align: left; border-bottom: 2px solid #ec4899;'>Item</th>");
        html.append("<th style='padding: 10px; text-align: center; border-bottom: 2px solid #ec4899;'>Qty</th>");
        html.append("<th style='padding: 10px; text-align: right; border-bottom: 2px solid #ec4899;'>Price</th>");
        html.append("</tr></thead><tbody>");

        for (OrderItem item : order.getOrderItems()) {
            html.append("<tr>");
            html.append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>")
                    .append(item.getProduct().getName());

            if (item.getPersonalizationDetails() != null && !item.getPersonalizationDetails().isEmpty()) {
                html.append("<br><small style='color: #666;'>✨ Personalized");
                Map<String, Object> details = item.getPersonalizationDetails();
                if (details.containsKey("custom_message")) {
                    html.append(" • Message: \"").append(details.get("custom_message")).append("\"");
                }
                html.append("</small>");
            }

            html.append("</td>");
            html.append("<td style='padding: 10px; text-align: center; border-bottom: 1px solid #ddd;'>")
                    .append(item.getQuantity()).append("</td>");
            html.append("<td style='padding: 10px; text-align: right; border-bottom: 1px solid #ddd;'>Rs ")
                    .append(String.format("%.2f", item.getPrice().doubleValue() * item.getQuantity())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append(
                "<div style='text-align: right; font-size: 18px; font-weight: bold; color: #ec4899; padding: 15px; background-color: #fce7f3; border-radius: 5px;'>");
        html.append("Total: Rs ").append(String.format("%.2f", order.getTotalPrice().doubleValue()));
        html.append("</div>");

        html.append(
                "<div style='margin-top: 20px; padding: 15px; background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 5px;'>");
        html.append("<p style='margin: 0;'><strong>📍 Delivery Address:</strong><br>")
                .append(order.getDeliveryAddress()).append("</p>");
        html.append("<p style='margin: 10px 0 0 0;'><strong>📞 Contact:</strong> ").append(order.getContactNumber())
                .append("</p>");
        html.append("</div>");

        html.append(
                "<p style='margin-top: 20px;'>We'll send you another email once your order is ready for delivery.</p>");
        html.append("<p style='margin-top: 20px;'>Best regards,<br><strong style='color: #ec4899;'>").append(fromName)
                .append("</strong></p>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    private String buildAdminNotificationHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h2 style='color: #10b981;'>New Order Received!</h2>");
        html.append("<p><strong>Order ID:</strong> #").append(order.getOrderId()).append("</p>");
        html.append("<p><strong>Customer:</strong> ").append(order.getUser().getUsername())
                .append(" (").append(order.getUser().getEmail()).append(")</p>");
        html.append("<p><strong>Total:</strong> Rs ").append(String.format("%.2f", order.getTotalPrice().doubleValue()))
                .append("</p>");
        html.append("<p><strong>Items:</strong> ").append(order.getOrderItems().size()).append("</p>");
        html.append("<p><strong>Delivery Address:</strong> ").append(order.getDeliveryAddress()).append("</p>");
        html.append("<p><strong>Contact:</strong> ").append(order.getContactNumber()).append("</p>");
        html.append("</body></html>");
        return html.toString();
    }

    private String buildStatusUpdateHtml(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h2 style='color: #ec4899;'>Order Status Update</h2>");
        html.append("<p>Dear ").append(order.getUser().getUsername()).append(",</p>");
        html.append("<p>Your order #").append(order.getOrderId())
                .append(" status has been updated to: <strong style='color: #10b981;'>")
                .append(order.getStatus()).append("</strong></p>");
        html.append("<p>Thank you for choosing ").append(fromName).append("!</p>");
        html.append("</body></html>");
        return html.toString();
    }
}
