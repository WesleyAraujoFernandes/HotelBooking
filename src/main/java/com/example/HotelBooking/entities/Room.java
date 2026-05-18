package com.example.HotelBooking.entities;

import java.math.BigDecimal;

import com.example.HotelBooking.enums.RoomType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Min(value = 1, message = "Room number must be greater than 0")
    @Column(unique = true)
    private Integer roomNumber;
    @Enumerated(EnumType.STRING)
    private RoomType roomType;
    @DecimalMin(value = "0.1", message = "Price per night must be greater than 0")
    private BigDecimal pricePerNight;
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;
    private String description;
    private String imageUrl;
}
