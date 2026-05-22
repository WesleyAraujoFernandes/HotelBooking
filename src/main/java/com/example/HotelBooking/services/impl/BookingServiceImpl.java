package com.example.HotelBooking.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.BookingStatus;
import com.example.HotelBooking.enums.PaymentStatus;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
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
    public Response findBookingByReferenceNo(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + bookingReference));
        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        return Response.builder()
                .status(200)
                .message("success")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response createBooking(BookingDTO bookingDTO) {
        User currentUser = userService.getCurrentLoggedInUser();
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
        String bookingReference = bookingCodeGenerator.generateBookingReference();
        // CREATE AND SAVE TO DATABASE
        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setBookingReference(bookingReference);
        booking.setBookingStatus(BookingStatus.BOOKED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDate.now());
        bookingRepository.save(booking);
        String paymentLink = "http://localhost:4200/payment/" + bookingReference + "/" + totalPrice;
        log.info("Booking created with reference: {}. Payment link: {}", bookingReference, paymentLink);
        
        // SEND EMAIL NOTIFICATION TO USER
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(currentUser.getEmail())
                .subject("Booking Confirmation - Reference: " + bookingReference)
                .body(String.format("Your Booking has been created successfully. \n "+
                "Please procced with your payment using the payment like below \n%s", paymentLink))
                .bookingReference(bookingReference)
                .build();
        notificationService.sendEmail(notificationDTO);
        return Response.builder()
                .status(200)
                .message("Booking created successfully")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response updateBooking(BookingDTO bookingDTO) {
        if (bookingDTO.getId() == null) {
            throw new NotFoundException("Booking ID is required for update");
        }
        Booking existingBooking = bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingDTO.getId()));
        if (bookingDTO.getBookingStatus() != null) {
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());
        }
        if(bookingDTO.getPaymentStatus() != null) {
            existingBooking.setPaymentStatus(bookingDTO.getPaymentStatus());
        }
        bookingRepository.save(existingBooking);
        return Response.builder()
                .status(200)
                .message("Booking updated successfully")
                .booking(bookingDTO)
                .build();
    }

    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO) {
        BigDecimal pricePerNight = room.getPricePerNight();
        long days  = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

}
