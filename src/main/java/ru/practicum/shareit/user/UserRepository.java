package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    private final AtomicLong generator = new AtomicLong(1);

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(generator.getAndIncrement());
        }

        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public void delete(Long id) {
        users.remove(id);
    }

    public boolean existsByEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return users.values()
                .stream()
                .anyMatch(user -> user.getEmail().equals(email) && !user.getId().equals(id));
    }
}
