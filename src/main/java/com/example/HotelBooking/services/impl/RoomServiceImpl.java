package com.example.HotelBooking.services.impl;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.RoomType;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.repositories.RoomRepository;
import com.example.HotelBooking.services.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    private static String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-image";

    @Override
    public Response addRoom(RoomDTO roomDTO, MultipartFile imageFile) {
        Room roomToSave = modelMapper.map(roomDTO, Room.class);
        if (imageFile != null) {
            String imagePath = saveImage(imageFile);
            roomToSave.setImageUrl(imagePath);
        }
        Room savedRoom = roomRepository.save(roomToSave);
        if (savedRoom != null) {
            return Response.builder().status(200).message("Room added successfully")
                    .room(modelMapper.map(savedRoom, RoomDTO.class)).build();
        } else {
            return Response.builder().status(500).message("Room not added").build();
        }
    }

    @Override
    public Response updateRoom(RoomDTO roomDTO, MultipartFile imageFile) {
        Room existingRoom = roomRepository.findById(roomDTO.getId())
                .orElseThrow(() -> new NotFoundException("Room does not exists"));
        if (existingRoom == null) {
            return Response.builder().status(404).message("Room not found").build();
        }
        modelMapper.map(roomDTO, existingRoom);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            existingRoom.setImageUrl(imagePath);
        }
        if (roomDTO.getRoomNumber() != null && roomDTO.getRoomNumber() >= 0) {
            existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        }
        if (roomDTO.getPricePerNight() != null && roomDTO.getPricePerNight().compareTo(BigDecimal.ZERO) >= 0) {
            existingRoom.setPricePerNight(roomDTO.getPricePerNight());
        }
        if (roomDTO.getCapacity() != null && roomDTO.getCapacity() > 0) {
            existingRoom.setCapacity(roomDTO.getCapacity());
        }
        if (roomDTO.getType() != null) {
            existingRoom.setType(roomDTO.getType());
        }
        if (roomDTO.getDescription() != null) {
            existingRoom.setDescription(roomDTO.getDescription());
        }
        Room updatedRoom = roomRepository.save(existingRoom);
        if (updatedRoom != null) {
            return Response.builder().status(200).message("Room updated successfully")
                    .room(modelMapper.map(updatedRoom, RoomDTO.class)).build();
        } else {
            return Response.builder().status(500).message("Room not updated").build();
        }
    }

    @Override
    public Response getAllRooms() {
        List<Room> rooms = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<RoomDTO> roomDTOList = modelMapper.map(rooms, new TypeToken<List<RoomDTO>>() {
        }.getType());
        rooms.forEach(room -> {
            log.info("Room ID: {}", room.getId());
            log.info("Room Number: {}", room.getRoomNumber());
            log.info("Room Type: {}", room.getType());
            log.info("Room Price Per Night: {}", room.getPricePerNight());
            log.info("Room Capacity: {}", room.getCapacity());
            log.info("Room Description: {}", room.getDescription());
        });
        return Response.builder().status(200).message("Rooms fetched successfully").rooms(roomDTOList).build();

    }

    @Override
    public Response getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room does not exists"));
        RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);
        return Response.builder().status(200).message("Room fetched successfully").room(roomDTO).build();
    }

    @Override
    public Response deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            return Response.builder().status(404).message("Room not found").build();
        } else {
            roomRepository.deleteById(id);
            return Response.builder().status(200).message("Room deleted successfully").build();
        }
    }

    @Override
    public Response getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingStateAndDateException("Check in date must be before today");
        }
        if (checkOutDate.isBefore(checkInDate)) {
            throw new InvalidBookingStateAndDateException("Check out date must be after check in date");
        }
        if (checkInDate.isEqual(checkOutDate)) {
            throw new InvalidBookingStateAndDateException("Check in date and check out date must be different");
        }
        List<Room> roomList = roomRepository.findAvailebleRooms(checkInDate, checkOutDate, roomType);
        List<RoomDTO> roomDTOList = modelMapper.map(roomList, new TypeToken<List<RoomDTO>>() {
        }.getType());
        return Response.builder().status(200).message("Rooms fetched successfully").rooms(roomDTOList).build();
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return Arrays.asList(RoomType.values());
    }

    @Override
    public Response searchRooms(String input) {
        List<Room> rooms = roomRepository.searchRooms("%" + input + "%");
        List<RoomDTO> roomDTOList = modelMapper.map(rooms, new TypeToken<List<RoomDTO>>() {
        }.getType());
        return Response.builder().status(200).message("Rooms fetched successfully").rooms(roomDTOList).build();
    }

    /** SAVE IMAGES TO DIRECTORY */
    private String saveImage(MultipartFile imageFile) {
        if (!imageFile.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image file is allwoed");
        }

        // Create directory if it does not exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Generate unique file name for the image
        String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        // Save the image to the directory
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;
        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to save image", e);
        }
        return imagePath;
    }
}
