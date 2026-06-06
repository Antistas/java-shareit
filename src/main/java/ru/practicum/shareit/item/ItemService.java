package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);
    ItemDto update(Long userId, Long itemId, ItemDto itemDto);
    ItemDto getById(Long userId, Long itemId);
    Collection<ItemDto> getOwnerItems(Long userId);
    Collection<ItemDto> search(Long userId, String text);
}
