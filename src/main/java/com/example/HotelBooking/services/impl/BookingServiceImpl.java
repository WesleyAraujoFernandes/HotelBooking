package com.example.HotelBooking.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.notifications.NotificationService;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.RoomRepository;
import com.example.HotelBooking.services.BookingCodeGenerator;
import com.example.HotelBooking.services.BookingService;
import com.example.HotelBooking.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final BookingCodeGenerator bookingCodeGenerator;

    @Override
    public Response getAllBookings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response findBookingByReferenceNo(String referenceNo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response createBooking(BookingDTO bookingDTO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response updateBooking(BookingDTO bookingDTO) {
        // TODO Auto-generated method stub
        return null;
    }

}
