package com.example.HotelBooking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.HotelBooking.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
