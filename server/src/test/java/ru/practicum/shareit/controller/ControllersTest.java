package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllersTest {

    @Test
    void shouldCallUserControllerMethods() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        UserDto dto = UserDto.builder().id(1L).name("User").email("u@mail.ru").build();

        when(service.create(dto)).thenReturn(dto);
        when(service.update(1L, dto)).thenReturn(dto);
        when(service.getById(1L)).thenReturn(dto);
        when(service.getAll()).thenReturn(List.of(dto));

        assertEquals(dto, controller.create(dto));
        assertEquals(dto, controller.update(1L, dto));
        assertEquals(dto, controller.getById(1L));
        assertEquals(1, controller.getAll().size());

        controller.delete(1L);
        verify(service).delete(1L);
    }

    @Test
    void shouldCallItemControllerMethods() {
        ItemService service = mock(ItemService.class);
        ItemController controller = new ItemController(service);

        ItemDto item = ItemDto.builder().id(1L).name("Item").description("Desc").available(true).build();
        CommentDto comment = CommentDto.builder().id(1L).text("Text").authorName("User").build();

        when(service.create(1L, item)).thenReturn(item);
        when(service.update(1L, 1L, item)).thenReturn(item);
        when(service.getById(1L, 1L)).thenReturn(item);
        when(service.getOwnerItems(1L)).thenReturn(List.of(item));
        when(service.search(1L, "item")).thenReturn(List.of(item));
        when(service.addComment(1L, 1L, comment)).thenReturn(comment);

        assertEquals(item, controller.create(1L, item));
        assertEquals(item, controller.update(1L, 1L, item));
        assertEquals(item, controller.getById(1L, 1L));
        assertEquals(1, controller.getOwnerItems(1L).size());
        assertEquals(1, controller.search(1L, "item").size());
        assertEquals(comment, controller.addComment(1L, 1L, comment));
    }

    @Test
    void shouldCallBookingControllerMethods() {
        BookingService service = mock(BookingService.class);
        BookingController controller = new BookingController(service);

        BookingCreateDto createDto = new BookingCreateDto();
        BookingDto dto = BookingDto.builder().id(1L).build();

        when(service.create(1L, createDto)).thenReturn(dto);
        when(service.approve(1L, 1L, true)).thenReturn(dto);
        when(service.getById(1L, 1L)).thenReturn(dto);
        when(service.getUserBookings(1L, BookingState.ALL)).thenReturn(List.of(dto));
        when(service.getOwnerBookings(1L, BookingState.WAITING)).thenReturn(List.of(dto));

        assertEquals(dto, controller.create(1L, createDto));
        assertEquals(dto, controller.approve(1L, 1L, true));
        assertEquals(dto, controller.getById(1L, 1L));
        assertEquals(1, controller.getUserBookings(1L, BookingState.ALL).size());
        assertEquals(1, controller.getOwnerBookings(1L, BookingState.WAITING).size());
    }

    @Test
    void shouldCallItemRequestControllerMethods() {
        ItemRequestService service = mock(ItemRequestService.class);
        ItemRequestController controller = new ItemRequestController(service);

        ItemRequestDto dto = ItemRequestDto.builder().id(1L).description("Need item").build();

        when(service.create(1L, dto)).thenReturn(dto);
        when(service.getOwnRequests(1L)).thenReturn(List.of(dto));
        when(service.getAllRequests(1L, 0, 10)).thenReturn(List.of(dto));
        when(service.getById(1L, 1L)).thenReturn(dto);

        assertEquals(dto, controller.create(1L, dto));
        assertEquals(1, controller.getOwnRequests(1L).size());
        assertEquals(1, controller.getAllRequests(1L, 0, 10).size());
        assertEquals(dto, controller.getById(1L, 1L));
    }
}
