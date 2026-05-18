package com.example.HotelBooking.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.HotelBooking.enums.BookingStatus;
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
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // @ManyToOne(cascade = CascadeType.REMOVE)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    // @ManyToOne(cascade = CascadeType.REMOVE)
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private String bookingReference;
    private LocalDate createdAt;
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
