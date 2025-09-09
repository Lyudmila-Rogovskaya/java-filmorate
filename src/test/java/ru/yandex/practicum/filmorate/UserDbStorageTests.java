package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.bd.UserDbStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
class UserDbStorageTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user1@example.com", "user1", "User One", Date.valueOf("1990-01-01"));

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user2@example.com", "user2", "User Two", Date.valueOf("1995-02-02"));
    }

    @Test
    void testAddFriend() {
        userStorage.addFriend(1L, 2L);

        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = 1 AND friend_id = 2";
        int count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "Дружба не добавлена");
    }

    @Test
    void testRemoveFriend() {
        userStorage.addFriend(1L, 2L);
        userStorage.removeFriend(1L, 2L);

        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = 1 AND friend_id = 2";
        int count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(0, count, "Дружба не удалена");
    }

    @Test
    void testGetFriends() {
        userStorage.addFriend(1L, 2L);
        List<User> friends = userStorage.getFriends(1L);

        assertEquals(1, friends.size(), "Неверное количество друзей");
        assertEquals(2L, friends.get(0).getId(), "Неверный ID друга");
    }

    @Test
    public void testCreateAndFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getLogin()).isEqualTo("testLogin");
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("original@example.com");
        user.setLogin("originalLogin");
        user.setName("Original Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        createdUser.setName("Updated Name");
        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testAddAndGetFriends() {
        User user1 = userStorage.create(createTestUser("user1@example.com", "user1"));
        User user2 = userStorage.create(createTestUser("user2@example.com", "user2"));

        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login + " Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    void confirmFriendship_ShouldUpdateStatus() {
        // Создание пользователей
        User user1 = createTestUser("user1@mail.com");
        User user2 = createTestUser("user2@mail.com");

        // Добавление в друзья
        userStorage.addFriend(user1.getId(), user2.getId());

        // Подтверждение дружбы
        userStorage.confirmFriend(user2.getId(), user1.getId());

        // Проверка статуса
        User updatedUser = userStorage.findById(user1.getId());
        assertThat(updatedUser.getFriends().get(user2.getId()))
                .isEqualTo(FriendshipStatus.CONFIRMED);
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin("login_" + email);
        user.setBirthday(LocalDate.now().minusYears(20));
        return userStorage.create(user);
    }

}
