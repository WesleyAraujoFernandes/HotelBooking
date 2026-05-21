package com.example.HotelBooking.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
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
        List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>() {}.getType());
        for (BookingDTO bookingDTO : bookingDTOList) {
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }
        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
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
        User user = userService.getCurrentLoggedInUser();
        Room room = roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + bookingDTO.getRoomId()));
        // VALIDATION: Ensure check-in date is not before today
        if (bookingDTO.getCheckInDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingStateAndDateException("Check-in date cannot be before today");
        }
        // VALIDATION: Ensure check-out date is after check-in date
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate())) {
            throw new InvalidBookingStateAndDateException("Check-out date cannot be before check-in date");
        }
        // VALIDATION: Ensure check-in date and check-out date are not the same
        if (bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate())) {
            throw new InvalidBookingStateAndDateException("Check-in date and check-out date cannot be the same");
        }

        // VALID ROOM AVAILABILITY CHECK
        boolean isAvailable = bookingRepository.isRoomAvailable(room.getId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (!isAvailable) {
            throw new InvalidBookingStateAndDateException("Room is not available for the selected dates");
        }

        BigDecimal totalPrice = calculateTotalPrice(room, bookingDTO);
    }

    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO) {
        long numberOfNights = bookingDTO.getCheckInDate().until(bookingDTO.getCheckOutDate()).getDays();
        return room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));
    }

}
