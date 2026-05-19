package com.example.HotelBooking.notifications;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.NotificationDTO;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmail'");
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
