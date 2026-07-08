package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void shouldCreateRequest() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(owner.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        BookingDto result = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());

        bookingRepository.deleteById(result.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldThrowWhenGetBookerIsNotExisting() {
        User booker = userRepository.save(User.builder()
                .name("booker")
                .email("booker2@google.ru")
                .build());
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(booker.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        assertThrows(ResponseStatusException.class, () -> bookingService.create(9999999L, bookingDto));
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldApproveBooking() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(owner.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        BookingDto result = bookingService.create(booker.getId(), bookingDto);
        result = bookingService.approve(owner.getId(), result.getId(), true);
        assertEquals(BookingStatus.APPROVED, result.getStatus());

        bookingRepository.deleteById(result.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldDeclineBooking() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(owner.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        BookingDto result = bookingService.create(booker.getId(), bookingDto);
        result = bookingService.approve(owner.getId(), result.getId(), false);
        assertEquals(BookingStatus.REJECTED, result.getStatus());

        bookingRepository.deleteById(result.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldThrowExceptionWhenApprovedByNotOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(owner.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        BookingDto result = bookingService.create(booker.getId(), bookingDto);

        assertThrows(ResponseStatusException.class,
                () -> bookingService.approve(booker.getId(), result.getId(), false));

        bookingRepository.deleteById(result.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldReturnBookingInfo() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        User third = userRepository.save(User.builder()
                .name("third")
                .email("third@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto item = itemService.create(owner.getId(), dto);
        BookingCreateDto bookingDto = createBookingDto(item.getId(), 1, 2);
        BookingDto result = bookingService.create(booker.getId(), bookingDto);

        assertEquals(result.getId(), bookingService.getById(owner.getId(), result.getId()).getId());
        assertEquals(result.getId(), bookingService.getById(booker.getId(), result.getId()).getId());
        assertThrows(ResponseStatusException.class,
                () -> bookingService.getById(third.getId(), result.getId()).getId());

        bookingRepository.deleteById(result.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
        userRepository.deleteById(third.getId());
    }

    @Test
    void shouldThrowWhenGetBookingsByUnknownUser() {
        assertThrows(ResponseStatusException.class,
                () -> bookingService.getUserBookings(999999L, BookingState.ALL));
    }

    @Test
    void shouldReturnCurrentBookings() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto itemDto = itemService.create(owner.getId(), dto);
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .booker(booker)
                        .item(item)
                        .start(LocalDateTime.now().minusHours(1))
                        .end(LocalDateTime.now().plusHours(1))
                        .status(BookingStatus.APPROVED)
                        .build()
        );

        Collection<BookingDto> result = bookingService.getUserBookings(booker.getId(), BookingState.CURRENT);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.iterator().next().getId());

        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldReturnPastBookings() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto itemDto = itemService.create(owner.getId(), dto);
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .booker(booker)
                        .item(item)
                        .start(LocalDateTime.now().minusDays(2))
                        .end(LocalDateTime.now().minusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build()
        );

        Collection<BookingDto> result =
                bookingService.getUserBookings(booker.getId(), BookingState.PAST);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.iterator().next().getId());

        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldReturnFutureBookings() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto itemDto = itemService.create(owner.getId(), dto);
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .booker(booker)
                        .item(item)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .status(BookingStatus.APPROVED)
                        .build()
        );

        Collection<BookingDto> result =
                bookingService.getUserBookings(booker.getId(), BookingState.FUTURE);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.iterator().next().getId());

        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldReturnOwnerWaitingBookings() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto itemDto = itemService.create(owner.getId(), dto);
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .booker(booker)
                        .item(item)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .status(BookingStatus.WAITING)
                        .build()
        );

        Collection<BookingDto> result =
                bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.iterator().next().getId());

        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    @Test
    void shouldReturnOwnerRejectedBookings() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@google.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@google.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();
        ItemDto itemDto = itemService.create(owner.getId(), dto);
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .booker(booker)
                        .item(item)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .status(BookingStatus.REJECTED)
                        .build()
        );

        Collection<BookingDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.REJECTED);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.iterator().next().getId());

        bookingRepository.deleteById(booking.getId());
        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(booker.getId());
    }

    private BookingCreateDto createBookingDto(Long itemId, int startDaysAfterNow, int endDaysAfterNow) {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(startDaysAfterNow));
        dto.setEnd(LocalDateTime.now().plusDays(endDaysAfterNow));
        return dto;
    }


}
