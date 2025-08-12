package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        userStorage.findById(user.getId());
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья: пользователь {} -> пользователь {}", userId, friendId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

    }

    public void removeFriend(Long userId, Long friendId) {

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        if (user.getFriends().contains(friendId)) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends().contains(userId)) {
            friend.getFriends().remove(userId);
        }

    }

    public List<User> getFriends(Long userId) {
        return userStorage.findById(userId).getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        Set<Long> commonIds = new HashSet<>(userStorage.findById(userId).getFriends());
        commonIds.retainAll(userStorage.findById(friendId).getFriends());
        return commonIds.stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public boolean emailExists(String email) {
        return userStorage.emailExists(email);
    }

}
