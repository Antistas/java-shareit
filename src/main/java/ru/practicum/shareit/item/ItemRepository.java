package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    private final AtomicLong generator = new AtomicLong(1);

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(generator.getAndIncrement());
        }

        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public Collection<Item> findAll() {
        return items.values();
    }

    public void delete(Long id) {
        items.remove(id);
    }

    public Collection<Item> search(String text) {
        String query = text.toLowerCase();
        return items.values()
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        item.getName().toLowerCase().contains(query)
                                || item.getDescription().toLowerCase().contains(query))
                .toList();
    }
}
