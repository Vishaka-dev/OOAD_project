package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.CheckoutRequestDTO;
import com.ooadproject.backend.entities.Order;
import com.ooadproject.backend.entities.Payment;
import com.ooadproject.backend.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPayment(Order order, CheckoutRequestDTO request) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.Pending);

        // Mock payment processing
        switch (request.getPaymentMethod()) {
            case credit_card:
                payment = processCreditCardPayment(payment, request);
                break;
            case payhear:
                payment = processPayhearPayment(payment);
                break;
            case cod:
                payment = processCODPayment(payment);
                break;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createPayment(Order order, Payment.PaymentMethod method, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setStatus(Payment.PaymentStatus.Pending);

        // Mock payment processing based on method
        switch (method) {
            case credit_card:
                payment.setStatus(Payment.PaymentStatus.Completed);
                payment.setTransactionId("CC_" + UUID.randomUUID().toString().substring(0, 8));
                break;
            case payhear:
                payment.setStatus(Payment.PaymentStatus.Completed);
                payment.setTransactionId("PH_" + UUID.randomUUID().toString().substring(0, 8));
                break;
            case cod:
                payment.setStatus(Payment.PaymentStatus.Completed);
                payment.setTransactionId("COD_" + UUID.randomUUID().toString().substring(0, 8));
                break;
        }

        return paymentRepository.save(payment);
    }

    private Payment processCreditCardPayment(Payment payment, CheckoutRequestDTO request) {
        // Mock credit card validation
        if (request.getCardNumber() != null && request.getCardNumber().length() >= 16) {
            payment.setStatus(Payment.PaymentStatus.Completed);
            payment.setTransactionId("CC_" + UUID.randomUUID().toString().substring(0, 8));
        } else {
            payment.setStatus(Payment.PaymentStatus.Failed);
        }
        return payment;
    }

    private Payment processPayhearPayment(Payment payment) {
        // Mock Payhear processing
        payment.setStatus(Payment.PaymentStatus.Completed);
        payment.setTransactionId("PH_" + UUID.randomUUID().toString().substring(0, 8));
        return payment;
    }

    private Payment processCODPayment(Payment payment) {
        // COD is always successful at order creation
        payment.setStatus(Payment.PaymentStatus.Completed);
        payment.setTransactionId("COD_" + UUID.randomUUID().toString().substring(0, 8));
        return payment;
    }
}