package com.example.HotelBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.notifications.NotificationService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class HotelBookingApplication {
	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(HotelBookingApplication.class, args);
	}

	@PostConstruct
	public void sendDummyEmail() {
		log.info("Send mail to:" + "wesley.a.fernandes@gmail.com");
		NotificationDTO notificationDTO = NotificationDTO.builder()
				.recipient("wesley.a.fernandes@gmail.com")
				.subject("Test Email")
				.body("This is a test email")
				.build();

		notificationService.sendEmail(notificationDTO);
	}

}
