package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    private final UserRepository userRepository = new UserRepository();
    private final UserService userService = new UserServiceImpl(userRepository);

    @Test
    void shouldCreateUser() {
        UserDto dto = UserDto.builder()
                .name("Rustam")
                .email("rustam@mail.ru")
                .build();

        UserDto result = userService.create(dto);

        assertNotNull(result.getId());
        assertEquals("Rustam", result.getName());
        assertEquals("rustam@mail.ru", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        userService.create(UserDto.builder()
                .name("User 1")
                .email("same@mail.ru")
                .build());

        UserDto duplicate = UserDto.builder()
                .name("User 2")
                .email("same@mail.ru")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.create(duplicate));
    }

    @Test
    void shouldGetAllUsers() {
        userService.create(UserDto.builder()
                .name("User 1")
                .email("user1@mail.ru")
                .build());

        userService.create(UserDto.builder()
                .name("User 2")
                .email("user2@mail.ru")
                .build());

        assertEquals(2, userService.getAll().size());
    }

    @Test
    void shouldUpdateUserNameAndEmail() {
        UserDto user = userService.create(UserDto.builder()
                .name("Old")
                .email("old@mail.ru")
                .build());

        UserDto update = UserDto.builder()
                .name("New")
                .email("new@mail.ru")
                .build();

        UserDto result = userService.update(user.getId(), update);

        assertEquals(user.getId(), result.getId());
        assertEquals("New", result.getName());
        assertEquals("new@mail.ru", result.getEmail());
    }

    @Test
    void shouldGetUserById() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("rustam@mail.ru")
                .build());

        UserDto result = userService.getById(user.getId());
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NoSuchElementException.class, () -> userService.getById(999L));
    }

    @Test
    void shouldDeleteUser() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("rustam@mail.ru")
                .build());

        userService.delete(user.getId());

        assertThrows(NoSuchElementException.class, () -> userService.getById(user.getId()));
    }
}