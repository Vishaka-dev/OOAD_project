package com.ooadproject.backend.controllers;//package com.ooadproject.backend.controllers;
//
//import com.ooadproject.backend.entities.Payment;
//import com.ooadproject.backend.services.PaymentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/payments")
//@RequiredArgsConstructor
//public class PaymentController {
//
//    private final PaymentService paymentService;
//
//    @GetMapping("/{orderId}")
//    public ResponseEntity<Payment> getPayment(@PathVariable Integer orderId) {
//        Optional<Payment> payment = paymentService.getByOrderId(orderId);
//        return payment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
//    }
//}
