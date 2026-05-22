package com.example.HotelBooking.payments.stripe;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private String bookingReference;
    private BigDecimal amount;

    private String transactionId;
    private boolean success;
    private String failureReason;
}
