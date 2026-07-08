package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappersTest {

    @Test
    void shouldMapUser() {
        User user = User.builder().id(1L).name("User").email("u@mail.ru").build();

        UserDto dto = UserMapper.toUserDto(user);
        User entity = UserMapper.toUser(dto);

        assertEquals(1L, dto.getId());
        assertEquals("User", entity.getName());
        assertEquals("u@mail.ru", entity.getEmail());
    }

    @Test
    void shouldMapItemWithAndWithoutRequest() {
        User owner = User.builder().id(1L).name("Owner").email("o@mail.ru").build();
        ItemRequest request = ItemRequest.builder().id(2L).description("Need").build();

        ItemDto dto = ItemDto.builder()
                .id(10L)
                .name("Item")
                .description("Desc")
                .available(true)
                .requestId(2L)
                .build();

        Item item = ItemMapper.toItem(dto, owner, request);
        ItemDto resultWithRequest = ItemMapper.toItemDto(item);

        item.setRequest(null);
        ItemDto resultWithoutRequest = ItemMapper.toItemDto(item);

        assertEquals("Item", item.getName());
        assertEquals(2L, resultWithRequest.getRequestId());
        assertNull(resultWithoutRequest.getRequestId());
    }

    @Test
    void shouldMapComment() {
        User author = User.builder().id(1L).name("Author").email("a@mail.ru").build();
        Item item = Item.builder().id(2L).name("Item").description("Desc").available(true).owner(author).build();

        CommentDto dto = CommentDto.builder().text("Good").build();
        Comment comment = ItemMapper.toComment(dto, item, author);
        CommentDto result = ItemMapper.toCommentDto(comment);

        assertEquals("Good", result.getText());
        assertEquals("Author", result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    @Test
    void shouldMapBooking() {
        User booker = User.builder().id(1L).name("Booker").email("b@mail.ru").build();
        User owner = User.builder().id(2L).name("Owner").email("o@mail.ru").build();
        Item item = Item.builder().id(3L).name("Item").description("Desc").available(true).owner(owner).build();

        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(3L);
        createDto.setStart(LocalDateTime.now().plusDays(1));
        createDto.setEnd(LocalDateTime.now().plusDays(2));

        Booking booking = BookingMapper.toBooking(createDto, item, booker);
        booking.setId(4L);

        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(4L, BookingMapper.toBookingDto(booking).getId());
        assertEquals("Item", BookingMapper.toBookingDto(booking).getItem().getName());
    }

    @Test
    void shouldMapItemRequest() {
        User user = User.builder().id(1L).name("User").email("u@mail.ru").build();
        ItemRequestDto dto = ItemRequestDto.builder().id(2L).description("Need item").build();

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        ItemRequestDto result = ItemRequestMapper.toDto(request, List.of());

        Item item = Item.builder().id(3L).name("Item").description("Desc").available(true).owner(user).build();

        assertEquals("Need item", result.getDescription());
        assertNotNull(request.getCreated());
        assertEquals(3L, ItemRequestMapper.toRequestItemDto(item).getId());
        assertEquals(1L, ItemRequestMapper.toRequestItemDto(item).getOwnerId());
    }
}
