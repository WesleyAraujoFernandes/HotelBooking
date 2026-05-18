package com.example.HotelBooking.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUtils {
    private static final long EXPIRATION_TIME_IN_MILSEC = 100L * 60L * 60L * 24L * 30L * 6L; // 6 months
    private SecretKey key;
    @Value("${secretJwtString}")
    private String secretJwtString;

    @PostConstruct
    private void init() {
        key = Keys.hmacShaKeyFor(secretJwtString.getBytes());
    }

}
