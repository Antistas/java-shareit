package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceImplTest {

    private final ItemRepository itemRepository = new ItemRepository();
    private final UserRepository userRepository = new UserRepository();
    private final ItemService itemService = new ItemServiceImpl(itemRepository, userRepository);

    @Test
    void shouldCreateItem() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        ItemDto result = itemService.create(owner.getId(), dto);

        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Мощная дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void shouldThrowWhenCreateItemWithUnknownUser() {
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> itemService.create(999L, dto));
    }

    @Test
    void shouldUpdateItemByOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Old")
                .description("Old description")
                .available(true)
                .build());

        ItemDto update = ItemDto.builder()
                .name("New")
                .description("New description")
                .available(false)
                .build();

        ItemDto result = itemService.update(owner.getId(), item.getId(), update);

        assertEquals("New", result.getName());
        assertEquals("New description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void shouldThrowWhenUpdateByNotOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        User other = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.ru")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build());

        assertThrows(ResponseStatusException.class,
                () -> itemService.update(other.getId(), item.getId(), ItemDto.builder().name("New").build()));
    }

    @Test
    void shouldThrowWhenUpdateByNotExistingItem() {
        User user = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.ru")
                .build());

        assertThrows(NoSuchElementException.class,
                () -> itemService.update(user.getId(), 999L, ItemDto.builder().name("New").build()));
    }

    @Test
    void shouldGetItemById() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build());

        ItemDto result = itemService.getById(owner.getId(), item.getId());

        assertEquals(item.getId(), result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    void shouldReturnOwnerItems() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Молоток")
                .description("Описание")
                .available(true)
                .build());

        Collection<ItemDto> items = itemService.getOwnerItems(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void shouldSearchOnlyAvailableItems() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель сломанная")
                .description("Недоступна")
                .available(false)
                .build());

        Collection<ItemDto> result = itemService.search(owner.getId(), "дрель");

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextBlank() {
        Collection<ItemDto> result = itemService.search(1L, "");
        assertTrue(result.isEmpty());
    }
}