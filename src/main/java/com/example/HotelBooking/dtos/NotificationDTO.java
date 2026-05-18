package com.example.HotelBooking.dtos;

import java.time.LocalDateTime;

import com.example.HotelBooking.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDTO {
    private Long id;
    @NotBlank(message = "Subject is required")
    private String subject;
    @NotBlank(message = "Recipient is required")
    private String recipient;
    private String body;

    private NotificationType notificationType;
    private String bookingReference;
    private LocalDateTime createdAt;
}
