package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Получен запрос на создание пользователя {}", userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.error("Email {} уже используется", userDto.getEmail());
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Получен запрос на обновление пользователя {} с id = {}", userDto, userId);
        User user = getUserById(userId);

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                log.error("Email уже используется");
                throw new IllegalArgumentException("Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        log.info("Получен запрос на получение пользователя с id = {}", userId);
        User user = getUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getAll() {
        log.info("Получен запрос на вывод всех пользователей");
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        log.info("Получен запрос на удаление пользователя с id = {}", userId);
        userRepository.delete(userId);
    }

    private void validateEmail(String email) {
        if (email.isBlank()) {
            log.error("Email не может быть пустым");
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (!email.contains("@")) {
            log.error("Некорректный email");
            throw new IllegalArgumentException("Некорректный email");
        }
    }
}