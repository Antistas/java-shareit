package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        log.info("Получен запрос на создание запроса вещи {} от пользователя {}", dto, userId);
        User user = getUser(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        return ItemRequestMapper.toDto(itemRequestRepository.save(request));
    }

    @Override
    public Collection<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Получен запрос на получение своих запросов пользователя {}", userId);
        getUser(userId);

        List<ItemRequest> requests = itemRequestRepository
                .findByRequestor_IdOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<RequestItemDto>> itemsByRequest = itemRepository
                .findByRequest_IdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(
                                ItemRequestMapper::toRequestItemDto,
                                Collectors.toList()
                        )
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public Collection<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получен запрос на получение всех запросов пользователя {} с {} в количестве {}", userId, from, size);
        if (size == null || size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр size должен быть больше 0");
        }
        int page = from / size;
        getUser(userId);

        return itemRequestRepository
                .findByRequestor_IdNotOrderByCreatedDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Получен запрос от gateway (getById) на получение запроса пользователя {} с id = {}", userId, requestId);
        getUser(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Запрос c id = " + requestId + " не найден"));

        var items = itemRepository.findByRequest_Id(requestId)
                .stream()
                .map(ItemRequestMapper::toRequestItemDto)
                .toList();

        return ItemRequestMapper.toDto(request, items);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь c id = " + userId + " не найден"));
    }
}
