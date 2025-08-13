package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> usedEmails = new HashSet<>();
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return users.values().stream()
                .sorted(Comparator.comparingLong(User::getId))
                .collect(Collectors.toList());
    }

    @Override
    public User create(User user) {
        if (usedEmails.contains(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        usedEmails.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }

        User oldUser = users.get(user.getId());

        if (user.getEmail() != null && !oldUser.getEmail().equals(user.getEmail())) {
            if (usedEmails.contains(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            usedEmails.remove(oldUser.getEmail());
            usedEmails.add(user.getEmail());
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public void delete(Long id) {
        User user = users.remove(id);
        if (user != null) {
            usedEmails.remove(user.getEmail());
        }
    }

    @Override
    public boolean emailExists(String email) {
        return usedEmails.contains(email);
    }

}
