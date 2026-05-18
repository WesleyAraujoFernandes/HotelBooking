package com.example.HotelBooking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.HotelBooking.entities.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

}
