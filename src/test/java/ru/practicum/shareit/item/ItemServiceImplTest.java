package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldCreateItem() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test@mail.ru")
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

        itemRepository.deleteById(result.getId());
        userRepository.deleteById(owner.getId());
    }

    @Test
    void shouldThrowWhenCreateItemWithUnknownUser() {
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> itemService.create(9999999999L, dto));
    }

    @Test
    void shouldUpdateItemByOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test@mail.ru")
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

        itemRepository.deleteById(result.getId());
        userRepository.deleteById(owner.getId());
    }

    @Test
    void shouldThrowWhenUpdateByNotOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test@mail.ru")
                .build());

        User other = userRepository.save(User.builder()
                .name("Other")
                .email("other_test@mail.ru")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build());

        assertThrows(ResponseStatusException.class,
                () -> itemService.update(other.getId(), item.getId(), ItemDto.builder().name("New").build()));

        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
        userRepository.deleteById(other.getId());
    }

    @Test
    void shouldThrowWhenUpdateByNotExistingItem() {
        User user = userRepository.save(User.builder()
                .name("Other")
                .email("other_test@mail.ru")
                .build());

        assertThrows(ResponseStatusException.class,
                () -> itemService.update(user.getId(), 99999999L, ItemDto.builder().name("New").build()));

        userRepository.deleteById(user.getId());
    }

    @Test
    void shouldGetItemById() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test@mail.ru")
                .build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build());

        ItemDto result = itemService.getById(owner.getId(), item.getId());
        assertEquals(item.getId(), result.getId());
        assertEquals("Дрель", result.getName());

        itemRepository.deleteById(item.getId());
        userRepository.deleteById(owner.getId());
    }

    @Test
    void shouldReturnOwnerItems() {

        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test@mail.ru")
                .build());

        ItemDto item1 = itemService.create(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(true)
                .build());

        ItemDto item2 = itemService.create(owner.getId(), ItemDto.builder()
                .name("Молоток")
                .description("Описание")
                .available(true)
                .build());

        Collection<ItemDto> items = itemService.getOwnerItems(owner.getId());
        assertEquals(2, items.size());

        itemRepository.deleteById(item1.getId());
        itemRepository.deleteById(item2.getId());
        userRepository.deleteById(owner.getId());
    }

    @Test
    void shouldSearchOnlyAvailableItems() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner_test224@mail.ru")
                .build());

        ItemDto item1 = itemService.create(owner.getId(), ItemDto.builder()
                .name("Вещь для теста Дрель")
                .description("Мощная")
                .available(true)
                .build());

        ItemDto item2 = itemService.create(owner.getId(), ItemDto.builder()
                .name("Тестовая вещь сломанная")
                .description("Недоступна")
                .available(false)
                .build());

        Collection<ItemDto> result = itemService.search(owner.getId(), "тест");
        assertEquals(1, result.size());

        itemRepository.deleteById(item1.getId());
        itemRepository.deleteById(item2.getId());
        userRepository.deleteById(owner.getId());
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextBlank() {
        Collection<ItemDto> result = itemService.search(1L, "");
        assertTrue(result.isEmpty());
    }
}