package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController { // для обслуживания пользователей
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> usedEmails = new HashSet<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> findAll() { // возвращаем всех пользователей
        log.info("Запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) { // создаем нового пользователя
        log.info("Запрос на создание пользователя: {}", user);

        // уникальность email
        if (usedEmails.contains(user.getEmail())) {
            log.warn("Email уже используется: {}", user.getEmail());
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        // логика для имени
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        // генерация id
        user.setId(nextId++);
        users.put(user.getId(), user);
        usedEmails.add(user.getEmail()); // cохраняем только email

        log.info("Пользователь создан: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) { // обновляем существующего пользователя
        log.info("Запрос на обновление пользователя: {}", newUser);

        // проверка id
        if (newUser.getId() == null) {
            log.warn("Id пользователя не указан");
            throw new ValidationException("Id должен быть указан");
        }

        // поиск существующего пользователя
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id={} не найден", newUser.getId());
            throw new NotFoundException("Пользователь не найден");
        }

        // обновление email
        if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
            if (usedEmails.contains(newUser.getEmail())) {
                log.warn("Email уже используется: {}", newUser.getEmail());
                throw new DuplicatedDataException("Этот имейл уже используется");
            }

            usedEmails.remove(oldUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            usedEmails.add(newUser.getEmail());
        }

        // обновление логина
        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }

        // обновление имени (если пусто - используем логин)
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName().isBlank() ?
                    oldUser.getLogin() :
                    newUser.getName());
        }

        // обновление даты рождения
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Пользователь обновлен: {}", oldUser);
        return oldUser;
    }

}
