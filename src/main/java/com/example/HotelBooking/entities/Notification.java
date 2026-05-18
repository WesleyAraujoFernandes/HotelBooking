package com.example.HotelBooking.entities;

import java.time.LocalDateTime;

import com.example.HotelBooking.enums.NotificationType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    @NotBlank(message = "Recipient is required")
    private String recipient;
    private String body;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    private String bookingReference;
    private final LocalDateTime createdAt = LocalDateTime.now();
}
