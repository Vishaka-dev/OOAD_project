package com.ooadproject.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        // Configure based on port
        if (mailPort == 465) {
            // SSL configuration for port 465
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", mailHost);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.starttls.enable", "false");
        } else {
            // TLS configuration for port 587 (works with SendGrid and most providers)
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.enable", "false");
            props.put("mail.smtp.ssl.trust", mailHost);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        }

        // Increased timeouts for cloud environments
        props.put("mail.smtp.connectiontimeout", "20000");
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.writetimeout", "20000");

        // Enable debug for troubleshooting (disable in production)
        props.put("mail.debug", "true");

        // Additional properties for better compatibility
        props.put("mail.smtp.socketFactory.fallback", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "false");

        return mailSender;
    }
}
