package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с id=" + userId + " не найден"));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи {} от пользователя {}", itemDto, userId);
        User owner = getUserById(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи {} c id = {} от пользователя {}", itemDto, itemId, userId);
        Item item = getItemById(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            log.error("Пользователь с id={} не является владельцем вещи", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с id=" + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        log.info("Получен запрос на отображение вещи c id = {}", itemId);

        Item item = getItemById(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(getComments(itemId));

        return itemDto;
    }

    @Override
    public Collection<ItemDto> getOwnerItems(Long userId) {
        log.info("Получен запрос на отображение вещей пользователя c id = {}", userId);
        getUserById(userId);
        return itemRepository.findByOwner_Id(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        log.info("Получен запрос на поиск вещей пользователя c id = {} с текстом {}", userId, text);
        if (text == null || text.isBlank()) {
            log.warn("Строка поиска пуста = {}", text);
            return List.of();
        }

        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Получен запрос на добавление комментария к вещи {} пользователя c id = {} с текстом {}",
                itemId, userId, commentDto);
        User author = getUserById(userId);
        Item item = getItemById(itemId);

        // пользователь арендовал вещь ранее
        boolean hasPastBooking = bookingRepository
                .existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                        itemId,
                        userId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now()
                );

        if (!hasPastBooking) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь не арендовал эту вещь");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentDto.builder()
                .id(saved.getId())
                .text(saved.getText())
                .authorName(saved.getAuthor().getName())
                .created(saved.getCreated())
                .build();
    }

    private List<CommentDto> getComments(Long itemId) {
        log.info("Получен запрос на вывод комментариев к вещи {}", itemId);
        return commentRepository.findByItem_Id(itemId)
                .stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .authorName(comment.getAuthor().getName())
                        .created(comment.getCreated())
                        .build())
                .toList();
    }
}
