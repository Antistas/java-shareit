package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        UserDto dto = UserDto.builder()
                .name("Rustam")
                .email("rustam_test@mail.ru")
                .build();

        UserDto result = userService.create(dto);
        assertNotNull(result.getId());
        assertEquals("Rustam", result.getName());
        assertEquals("rustam_test@mail.ru", result.getEmail());
        userService.delete(result.getId());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserDto result = userService.create(UserDto.builder()
                .name("User 1")
                .email("same_test@mail.ru")
                .build());

        UserDto duplicate = UserDto.builder()
                .name("User 2")
                .email("same_test@mail.ru")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.create(duplicate));
        userService.delete(result.getId());
    }

    @Test
    void shouldGetAllUsers() {
        int currentSize = userService.getAll().size();

        UserDto result1 = userService.create(UserDto.builder()
                .name("User 1")
                .email("user1_test@mail.ru")
                .build());

        UserDto result2 = userService.create(UserDto.builder()
                .name("User 2")
                .email("user2_test@mail.ru")
                .build());

        assertEquals(currentSize + 2, userService.getAll().size());
        userService.delete(result1.getId());
        userService.delete(result2.getId());
    }

    @Test
    void shouldUpdateUserNameAndEmail() {
        UserDto user = userService.create(UserDto.builder()
                .name("Old")
                .email("old_test@mail.ru")
                .build());

        UserDto update = UserDto.builder()
                .name("New")
                .email("new_test@mail.ru")
                .build();

        UserDto result = userService.update(user.getId(), update);

        assertEquals(user.getId(), result.getId());
        assertEquals("New", result.getName());
        assertEquals("new_test@mail.ru", result.getEmail());
        userService.delete(result.getId());
    }

    @Test
    void shouldGetUserById() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("rustam_test@mail.ru")
                .build());

        UserDto result = userService.getById(user.getId());
        assertEquals(user.getId(), result.getId());
        userService.delete(result.getId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NoSuchElementException.class, () -> userService.getById(99999999L));
    }

    @Test
    void shouldDeleteUser() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("rustam_test@mail.ru")
                .build());

        userService.delete(user.getId());
        assertThrows(NoSuchElementException.class, () -> userService.getById(user.getId()));
    }

    @Test
    void shouldUpdateOnlyName() {
        UserDto user = userService.create(UserDto.builder()
                .name("Old")
                .email("only_name_test@mail.ru")
                .build());

        UserDto update = UserDto.builder()
                .name("New")
                .build();

        UserDto result = userService.update(user.getId(), update);

        assertEquals(user.getId(), result.getId());
        assertEquals("New", result.getName());
        assertEquals("only_name_test@mail.ru", result.getEmail());

        userService.delete(result.getId());
    }

    @Test
    void shouldUpdateOnlyEmail() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("only_email_old@mail.ru")
                .build());

        UserDto update = UserDto.builder()
                .email("only_email_new@mail.ru")
                .build();

        UserDto result = userService.update(user.getId(), update);

        assertEquals(user.getId(), result.getId());
        assertEquals("Rustam", result.getName());
        assertEquals("only_email_new@mail.ru", result.getEmail());

        userService.delete(result.getId());
    }

    @Test
    void shouldThrowWhenUpdateUnknownUser() {
        UserDto update = UserDto.builder()
                .name("Unknown")
                .email("unknown_update@mail.ru")
                .build();

        assertThrows(NoSuchElementException.class,
                () -> userService.update(99999999L, update));
    }

    @Test
    void shouldThrowWhenUpdateEmailAlreadyExists() {
        UserDto user1 = userService.create(UserDto.builder()
                .name("User 1")
                .email("busy_email_1@mail.ru")
                .build());

        UserDto user2 = userService.create(UserDto.builder()
                .name("User 2")
                .email("busy_email_2@mail.ru")
                .build());

        UserDto update = UserDto.builder()
                .email("busy_email_1@mail.ru")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(user2.getId(), update));

        userService.delete(user1.getId());
        userService.delete(user2.getId());
    }

    @Test
    void shouldNotChangeUserWhenUpdateEmptyDto() {
        UserDto user = userService.create(UserDto.builder()
                .name("Rustam")
                .email("empty_update@mail.ru")
                .build());

        UserDto update = UserDto.builder().build();

        UserDto result = userService.update(user.getId(), update);

        assertEquals(user.getId(), result.getId());
        assertEquals("Rustam", result.getName());
        assertEquals("empty_update@mail.ru", result.getEmail());

        userService.delete(result.getId());
    }
}