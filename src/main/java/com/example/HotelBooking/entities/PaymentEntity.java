package com.example.HotelBooking.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.HotelBooking.enums.PaymentGatway;
import com.example.HotelBooking.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private PaymentGatway paymentGatway;
    private LocalDateTime paymentDate;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String bookingReference;
    private String failureReason;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
