package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
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

        findById(userId);
        findById(friendId);

        log.info("Добавление в друзья: пользователь {} -> пользователь {}", userId, friendId);
        friendshipStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        friendshipStorage.removeFriend(userId, friendId); // Делегируем хранилищу
    }

    public List<User> getFriends(Long userId) {
        findById(userId);
        return friendshipStorage.getFriends(userId); // Делегируем хранилищу
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);
        return friendshipStorage.getCommonFriends(userId, otherId); // Делегируем хранилищу
    }

    public User findById(Long id) {
        log.info("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id);
    }

}
