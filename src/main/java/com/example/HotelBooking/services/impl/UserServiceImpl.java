package com.example.HotelBooking.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.LoginRequest;
import com.example.HotelBooking.dtos.RegistrationRequest;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.UserDTO;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.UserRole;
import com.example.HotelBooking.exceptions.InvalidCredentialsException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.UserRepository;
import com.example.HotelBooking.security.JwtUtils;
import com.example.HotelBooking.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    @Override
    public Response registerUser(RegistrationRequest registrationRequest) {
        UserRole role = UserRole.CUSTOMER;
        if (registrationRequest.getRole() != null) {
            role = registrationRequest.getRole();
        }
        User userToSave = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .active(true)
                .build();
        User user = userRepository.save(userToSave);
        // String token = jwtUtils.generateToken(user);
        return Response.builder().status(200).message("User registered successfully").build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        log.info("INSIDE loginUser()");
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Password does not match");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        return Response.builder()
                .status(200)
                .message("User logged in successfully")
                .role(user.getRole())
                .token(token)
                .active(user.isActive())
                .expirationTime("6 month")
                .build();
    }

    @Override
    public Response getAllUsers() {
        log.info("INSIDE getAllUsers");

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<UserDTO> userDTOList = modelMapper.map(users, new TypeToken<List<UserDTO>>() {
        }.getType());
        return Response.builder()
                .status(200)
                .message("Users fetched successfully")
                .users(userDTOList)
                .build();
    }

    @Override
    public Response getOwnAccountDetails() {
        log.info("INSIDE getOwnAccountDetails");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return Response.builder()
                .status(200)
                .message("User details fetched successfully")
                .user(userDTO)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        log.info("INSIDE getCurrentLoggedInUser");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return user;
    }

    @Override
    public Response updateOwnAccount(UserDTO userDTO) {
        log.info("INSIDE updateOwnAccount");
        User existingUser = getCurrentLoggedInUser();
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getFirstName() != null) {
            existingUser.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            existingUser.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        User updatedUser = userRepository.save(existingUser);
        UserDTO updatedUserDTO = modelMapper.map(updatedUser, UserDTO.class);
        return Response.builder()
                .status(200)
                .message("User details updated successfully")
                .user(updatedUserDTO)
                .build();

    }

    @Override
    public Response deleteOwnAccount() {
        log.info("INSIDE deleteOwnAccount");
        User currentUser = getCurrentLoggedInUser();
        currentUser.setActive(false);
        User deletedUser = userRepository.save(currentUser);
        UserDTO deletedUserDTO = modelMapper.map(deletedUser, UserDTO.class);
        return Response.builder()
                .status(200)
                .message("User deleted successfully")
                .user(deletedUserDTO)
                .build();
    }

    @Override
    public Response getMyBookingHistory() {
        log.info("INSIDE getMyBookingHistory");
        User currentUser = getCurrentLoggedInUser();
        List<Booking> bookingList = bookingRepository.findByUserId(currentUser.getId());
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>() {
        }.getType());
        return Response.builder()
                .status(200)
                .message("Booking history fetched successfully")
                .bookings(bookingDTOList)
                .build();
    }

}
