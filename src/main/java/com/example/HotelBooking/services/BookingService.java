package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.Response;

public interface BookingService {
    Response getAllBookings();
    Response findBookingByReferenceNo(String referenceNo);
    Response createBooking(BookingDTO bookingDTO);
    Response updateBooking(BookingDTO bookingDTO);
}
