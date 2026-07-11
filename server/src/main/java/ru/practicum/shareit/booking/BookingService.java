package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {

    BookingDto create(Long userId, BookingCreateDto dto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    Collection<BookingDto> getUserBookings(Long userId, BookingState state);

    Collection<BookingDto> getOwnerBookings(Long userId, BookingState state);
}