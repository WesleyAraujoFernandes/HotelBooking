package com.example.HotelBooking.notifications;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.entities.Notification;
import com.example.HotelBooking.enums.NotificationType;
import com.example.HotelBooking.repositories.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO) {
        log.info("Sending Email...");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("wesley.a.fernandes@gmail.com");
        simpleMailMessage.setTo(notificationDTO.getRecipient());
        simpleMailMessage.setSubject(notificationDTO.getSubject());
        simpleMailMessage.setText(notificationDTO.getBody());
        javaMailSender.send(simpleMailMessage);

        // Save to database
        Notification notificationToSave = Notification.builder()
                .recipient(notificationDTO.getRecipient())
                .subject(notificationDTO.getSubject())
                .body(notificationDTO.getBody())
                .type(NotificationType.EMAIL)
                .bookingReference(notificationDTO.getBookingReference())
                .build();
        notificationRepository.save(notificationToSave);
    }

    @Override
    public void sendSms() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendSms'");
    }

    @Override
    public void sendWhatsapp() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendWhatsapp'");
    }

}
