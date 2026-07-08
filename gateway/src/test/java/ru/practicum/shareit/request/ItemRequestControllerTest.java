package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Нужна дрель");

        when(itemRequestClient.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());

        verify(itemRequestClient).create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(itemRequestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getOwnRequests(1L);
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(itemRequestClient.getAllRequests(1L, 0, 10))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllRequests(1L, 0, 10);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(itemRequestClient.getById(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/2")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestClient).getById(1L, 2L);
    }
}
