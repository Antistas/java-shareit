package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // создаем бронирование
    @Override
    public BookingDto create(Long userId, BookingCreateDto dto) {
        log.info("Получен запрос на создание бронирования {} от пользователя {}", dto, userId);
        User user = getUser(userId);
        Item item = getItem(dto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя бронировать свою вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь недоступна");
        }

        validateDates(dto.getStart(), dto.getEnd());

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    // владелец подтверждает или отклоняет бронирование
    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Получен запрос на подтверждение бронирования {} от пользователя {} со статусом {}",
                bookingId, userId, approved);

        Booking booking = getBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Подтвердить может только владелец");
        }
        getUser(userId);

        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    // достаем информацию о бронировании
    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        log.info("Получен запрос на получение информации о бронирования {} от пользователя {}",
                bookingId, userId);
        getUser(userId);
        Booking booking = getBooking(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет доступа к бронированию");
        }

        return BookingMapper.toBookingDto(booking);
    }

    // получаем информацию о бронированиях пользователем
    @Override
    public Collection<BookingDto> getUserBookings(Long userId, BookingState state) {
        log.info("Получен запрос на получение информации о бронированиях пользователя {} со статусом {}",
                userId, state);
        getUser(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, sort);
            case CURRENT -> bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    // получаем информацию о бронированиях (владелец)
    @Override
    public Collection<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        log.info("Получен запрос на получение информации о бронированиях у пользователя {} со статусом {}",
                userId, state);
        getUser(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> bookings =  switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, sort);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, sort);
            default -> bookingRepository.findByItem_Owner_Id(userId, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    // хелперы
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start) || start.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные даты бронирования");
        }
    }
}
