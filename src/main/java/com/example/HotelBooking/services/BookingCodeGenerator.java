package com.example.HotelBooking.services;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.BookingReference;
import com.example.HotelBooking.repositories.BookingReferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingCodeGenerator {
    private final BookingReferenceRepository bookingReferenceRepository;
    public String generateBookingReference() {
        String bookingReference;
        do {
            bookingReference = generateRamdomAlphanumericCode(10);
        } while (isBookingReferenceExists(bookingReference));
        saveBookingReferenceToDatabase(bookingReference);
        return bookingReference;
    }

    private String generateRamdomAlphanumericCode(int lenght) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(lenght);
        for (int i = 0; i < lenght; i++) {
            int index = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(index));
        }
        return stringBuilder.toString();
    }

    private boolean isBookingReferenceExists(String bookingReference) {
        return bookingReferenceRepository.findByReferenceNo(bookingReference).isPresent();
    }

    private void saveBookingReferenceToDatabase(String bookingReference) {
        BookingReference bookingReferenceToSave = BookingReference.builder()
                .referenceNo(bookingReference)
                .build();
        bookingReferenceRepository.save(bookingReferenceToSave);
    }
}
