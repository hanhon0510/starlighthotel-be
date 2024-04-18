package com.hieu.starlighthotel.service;

import com.hieu.starlighthotel.model.BookedRoom;

import java.util.List;

public interface BookingService {
    List<BookedRoom> getAllBookingsByRoomId(Long roomId);
}
