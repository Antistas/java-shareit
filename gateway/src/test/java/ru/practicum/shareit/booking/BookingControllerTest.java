package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldCreateBooking() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(1L), any(BookItemRequestDto.class));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approve(1L, 2L, true))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/2")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(1L, 2L, true);
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getBooking(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/2")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getBooking(1L, 2L);
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        when(bookingClient.getBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingClient.getOwnerBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(1L, BookingState.ALL);
    }
}