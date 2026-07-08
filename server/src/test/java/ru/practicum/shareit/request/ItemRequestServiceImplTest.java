package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void shouldCreateRequest() {
        User user = userRepository.save(User.builder()
                .name("Owner")
                .email("rustam_test@mail.ru")
                .build());


        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto result = itemRequestService.create(user.getId(), dto);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());

        itemRequestRepository.deleteById(result.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void shouldNotCreateRequestWhenUserIsNotExist() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();
        assertThrows(ResponseStatusException.class, () -> itemRequestService.create(9999999999L, dto));
    }

    @Test
    void shouldReturnOwnRequests() {
        User user = userRepository.save(User.builder()
                .name("Owner")
                .email("rustam_test@mail.ru")
                .build());


        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        ItemRequestDto dto1 = itemRequestService.create(user.getId(), dto);
        ItemRequestDto dto2 = itemRequestService.create(user.getId(), dto);
        ItemRequestDto dto3 = itemRequestService.create(user.getId(), dto);

        assertEquals(3, itemRequestService.getOwnRequests(user.getId()).size());

        itemRequestRepository.deleteById(dto1.getId());
        itemRequestRepository.deleteById(dto2.getId());
        itemRequestRepository.deleteById(dto3.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void shouldReturnEmptyListOwnRequests() {
        User user = userRepository.save(User.builder()
                .name("Owner")
                .email("rustam_empty_list_test@mail.ru")
                .build());

        assertEquals(List.of(), itemRequestService.getOwnRequests(user.getId()));
        userRepository.deleteById(user.getId());
    }

    @Test
    void shouldNotReturnRequestsWhenUserIsNotExist() {
        assertThrows(ResponseStatusException.class, () -> itemRequestService.getOwnRequests(9999999999L));
    }

    @Test
    void shouldReturnPages() {
        User currentUser = userRepository.save(User.builder()
                .name("Current user")
                .email("current_user@mail.ru")
                .build());

        User requestAuthor = userRepository.save(User.builder()
                .name("Request author")
                .email("request_author@mail.ru")
                .build());

        ArrayList<ItemRequestDto> createdRequests = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            createdRequests.add(itemRequestService.create(requestAuthor.getId(), ItemRequestDto.builder()
                    .description("Запрос №" + i)
                    .build()));
        }

        List<ItemRequestDto> result = itemRequestService.getAllRequests(currentUser.getId(), 2, 2)
                .stream()
                .toList();

        List<Long> actualIds = result.stream()
                .map(ItemRequestDto::getId)
                .toList();

        List<Long> expectedIds = List.of(
                createdRequests.get(3).getId(),
                createdRequests.get(2).getId()
        );

        assertEquals(2, result.size());
        assertEquals(expectedIds, actualIds);

        itemRequestRepository.deleteAllById(createdRequests.stream()
                        .map(ItemRequestDto::getId)
                        .toList()
        );
        userRepository.deleteById(currentUser.getId());
        userRepository.deleteById(requestAuthor.getId());
    }

    @Test
    void shouldReturnErrorWhenSizeZero() {
        assertThrows(ResponseStatusException.class, () -> itemRequestService.getAllRequests(999L, 2, 0));
    }

    @Test
    void shouldReturnErrorWhenSizeLessZero() {
        assertThrows(ResponseStatusException.class, () -> itemRequestService.getAllRequests(999L, 2, -1));
    }

}
