# Email Service Setup Guide

## Overview

The email service has been implemented to send automated notifications for order-related events. It supports HTML-formatted emails with professional styling.

## Features

- ✅ Order confirmation emails to customers
- ✅ Order status update notifications
- ✅ Admin notifications for new orders
- ✅ Order cancellation notifications
- ✅ Delivery notifications
- ✅ HTML-formatted emails with responsive design
- ✅ Configurable email settings
- ✅ Email service enable/disable toggle

## Configuration

### 1. Email Provider Setup (Gmail Example)

Update `application.yaml` with your email credentials:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_gmail@gmail.com
    password: your_app_password # Use App Password for Gmail
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000

app:
  mail:
    admin: admin@example.com
    from-name: "Gift Shop Team"
    enabled: true
```

### 2. Gmail App Password Setup

1. Enable 2-Factor Authentication on your Gmail account
2. Go to Google Account settings > Security > App passwords
3. Generate an app password for "Mail"
4. Use this app password in the configuration (not your regular password)

### 3. Other Email Providers

For other providers, update the SMTP settings accordingly:

**Outlook/Hotmail:**

```yaml
spring:
  mail:
    host: smtp-mail.outlook.com
    port: 587
```

**Yahoo:**

```yaml
spring:
  mail:
    host: smtp.mail.yahoo.com
    port: 587
```

## Email Types

### 1. Order Confirmation

- Sent when an order is successfully created and payment is completed
- Includes order details, items, personalization, and delivery information
- Also sends notification to admin

### 2. Order Status Updates

- Sent when order status changes (Confirmed, Shipped, Delivered, Cancelled)
- Includes current status and relevant information

### 3. Admin Notifications

- Sent to admin email when new orders are received
- Includes complete order information for processing

### 4. Order Cancellation

- Sent when an order is cancelled
- Includes cancellation details and support information

### 5. Delivery Notifications

- Sent when order is out for delivery
- Includes delivery address and contact information

## Usage in Code

The email service is automatically integrated with the order system:

```java
// In OrderService.java - automatically called
emailService.sendOrderConfirmation(order);
emailService.sendOrderStatusUpdate(order);

// Additional methods available
emailService.sendOrderCancellation(order);
emailService.sendDeliveryNotification(order);
```

## Testing

### Enable/Disable Email Service

Set `app.mail.enabled: false` in `application.yaml` to disable email sending during development.

### Test Email Configuration

You can test the email configuration by:

1. Creating a test order through the API
2. Checking the application logs for email sending status
3. Verifying emails are received

## Troubleshooting

### Common Issues

1. **Authentication Failed**

   - Ensure you're using an App Password for Gmail
   - Check that 2FA is enabled on your email account

2. **Connection Timeout**

   - Verify SMTP host and port settings
   - Check firewall/network restrictions

3. **Emails Not Sending**
   - Check `app.mail.enabled` is set to `true`
   - Review application logs for error messages
   - Verify email addresses are valid

### Logs

The service logs all email operations:

- Successful sends: `INFO` level
- Failures: `ERROR` level with exception details
- Disabled service: `INFO` level

## Security Notes

- Never commit real email credentials to version control
- Use environment variables for production:
  ```yaml
  spring:
    mail:
      username: ${EMAIL_USERNAME}
      password: ${EMAIL_PASSWORD}
  ```
- Consider using a dedicated email service (SendGrid, AWS SES) for production

## Customization

### Email Templates

Email templates are built using HTML in the `EmailService` class. You can customize:

- Colors and styling in the CSS sections
- Email content and messaging
- Company branding and logos (add as base64 images)

### Adding New Email Types

1. Add new method in `EmailService`
2. Create corresponding HTML template method
3. Call the method from appropriate service classes
4. Update this documentation

## Production Recommendations

1. Use a dedicated email service provider
2. Implement email queuing for high volume
3. Add email templates as external files
4. Implement email tracking and analytics
5. Add email preferences for users
6. Consider implementing email retry logic

