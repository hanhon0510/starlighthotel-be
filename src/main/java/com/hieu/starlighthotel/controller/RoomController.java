package com.hieu.starlighthotel.controller;

import com.hieu.starlighthotel.exeption.PhotoRetrievalException;
import com.hieu.starlighthotel.model.BookedRoom;
import com.hieu.starlighthotel.model.Room;
import com.hieu.starlighthotel.response.BookingResponse;
import com.hieu.starlighthotel.response.RoomResponse;
import com.hieu.starlighthotel.service.BookingService;
import com.hieu.starlighthotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {


    private final RoomService roomService;
    private final BookingService bookingService;


    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);



        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<RoomResponse> allRoomsResponse = new ArrayList<>();
        List<Room> allRooms =  roomService.getAllRooms();
        for (Room room : allRooms) {
            byte[] photoByte = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoByte != null && photoByte.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoByte);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                allRoomsResponse.add(roomResponse);
            }
//            RoomResponse response = new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice());
//            allRoomsResponse.add(response);
        }

        return new ResponseEntity<>(allRoomsResponse, HttpStatus.OK);
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(
                    booking -> new BookingResponse(
                            booking.getBookingId(),
                            booking.getCheckInDate(),
                            booking.getCheckOutDate(),
                            booking.getBookingConfirmationCode())
                )
                .toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch(SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");

            }
        }

        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(),photoBytes, bookingInfo);

    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }


}
