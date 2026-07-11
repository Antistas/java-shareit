package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        when(itemClient.create(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Новая дрель");

        when(itemClient.update(eq(1L), eq(2L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/2")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).update(eq(1L), eq(2L), any(ItemDto.class));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemClient.getById(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/2")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).getById(1L, 2L);
    }

    @Test
    void shouldGetOwnerItems() throws Exception {
        when(itemClient.getOwnerItems(1L))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).getOwnerItems(1L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.search(1L, "дрель"))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).search(1L, "дрель");
    }
}