package com.example.HotelBooking.payments.stripe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HotelBooking.dtos.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    // Define endpoints for payment processing, e.g., create payment intent, confirm payment, etc.
    @PostMapping("/pay")
    public ResponseEntity<Response> initializePayment(@RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.ok(paymentService.initializePayment(paymentRequest));
    }

    @PutMapping("/update")
    public void updatePayment(@RequestBody PaymentRequest paymentRequest) {
        paymentService.updatePaymentBooking(paymentRequest);
    }
}
