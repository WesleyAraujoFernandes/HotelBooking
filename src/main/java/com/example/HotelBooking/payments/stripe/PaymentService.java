package com.example.HotelBooking.payments.stripe;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.BookingReference;
import com.example.HotelBooking.entities.PaymentEntity;
import com.example.HotelBooking.enums.NotificationType;
import com.example.HotelBooking.enums.PaymentGatway;
import com.example.HotelBooking.enums.PaymentStatus;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.notifications.NotificationService;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService emailService;

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    public Response createPaymentIntent(PaymentRequest paymentRequest) {
        log.info("Inside createPaymentIntent with bookingReference: {}", paymentRequest.getBookingReference());
        Stripe.apiKey = secretKey;

        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found with reference: " + bookingReference));
        if (booking.getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
            throw new NotFoundException("Payment already completed for this booking: " + bookingReference);
        }

        if (booking.getTotalPrice().compareTo(paymentRequest.getAmount()) != 0) {
            throw new NotFoundException("Payment amount does not match booking total price for reference: " + bookingReference);
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                    .setCurrency("usd")
                    .setDescription("Payment for booking: " + bookingReference)
                    .putMetadata("bookingReference", bookingReference)
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            String uniquetransactionId = intent.getClientSecret();
            return Response.builder()
                    .status(200)
                    .message("Payment intent created successfully")
                    .transactionId(uniquetransactionId)
                    .build();
            
        } catch (Exception e) {
            throw new NotFoundException("Error occurred while creating payment intent for booking: " + bookingReference);
        }
    }

    public void updatePaymentBooking(PaymentRequest paymentRequest) {
        log.info("Inside updatePaymentBooking with bookingReference: {}", paymentRequest.getBookingReference());
        Booking booking = bookingRepository.findByBookingReference(paymentRequest.getBookingReference())
                .orElseThrow(() -> new NotFoundException("Booking not found with reference: " + paymentRequest.getBookingReference()));
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentGatway(PaymentGatway.STRIPE);
        payment.setAmount(paymentRequest.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentStatus(paymentRequest.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingReference(paymentRequest.getBookingReference());
        payment.setUser(booking.getUser());
        if (!paymentRequest.isSuccess()) {
            payment.setFailureReason(paymentRequest.getFailureReason());
        }
        paymentRepository.save(payment);

        // create and send notification via email
        String subject = "Payment " + (paymentRequest.isSuccess() ? "Successful" : "Failed") + " for Booking: " + paymentRequest.getBookingReference();
        String message = "Dear " + booking.getUser().getFirstName() + ",\n\nYour payment for booking reference " + paymentRequest.getBookingReference() + " has been " + (paymentRequest.isSuccess() ? "successful" : "failed") + ".\n\n";
        if (!paymentRequest.isSuccess()) {
            message += "Reason for failure: " + paymentRequest.getFailureReason() + "\n\n";
        }
        message += "Thank you for choosing our hotel booking service.\n\nBest regards,\nHotel Booking Team";
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(booking.getUser().getEmail())
                .notificationType(NotificationType.EMAIL)
                .bookingReference(paymentRequest.getBookingReference())
                .subject(subject)
                .body(message)
                .build();
        if (paymentRequest.isSuccess()) {
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking);
            notificationDTO.setSubject("Payment Successful for Booking: " + paymentRequest.getBookingReference());
            notificationDTO.setBody("Dear " + booking.getUser().getFirstName() + ",\n\nYour payment for booking reference " + paymentRequest.getBookingReference() + " has been successful.\n\nThank you for choosing our hotel booking service.\n\nBest regards,\nHotel Booking Team");
            emailService.sendEmail(notificationDTO);
        } else {
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            notificationDTO.setSubject("Payment Failed for Booking: " + paymentRequest.getBookingReference());
            notificationDTO.setBody("Dear " + booking.getUser().getFirstName() + ",\n\nYour payment for booking reference " + paymentRequest.getBookingReference() + " has failed.\n\nReason for failure: " + paymentRequest.getFailureReason() + "\n\nThank you for choosing our hotel booking service.\n\nBest regards,\nHotel Booking Team");
            emailService.sendEmail(notificationDTO);
        }
    }
}
