package ru.yandex.practicum.filmorate.storage.bd;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@ActiveProfiles("test")
@Import({UserDbStorage.class, FriendshipDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTests {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userStorage;
    private final FriendshipDbStorage friendshipStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }

    @Test
    void testFindUserById() {
        User newUser = new User();
        newUser.setEmail("test@mail.ru");
        newUser.setLogin("testLogin");
        newUser.setName("Test User");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(newUser);

        User foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", "test@mail.ru")
                .hasFieldOrPropertyWithValue("login", "testLogin")
                .hasFieldOrPropertyWithValue("name", "Test User");
    }

    @Test
    void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("new@mail.ru");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(newUser);

        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "new@mail.ru")
                .hasFieldOrPropertyWithValue("login", "newuser")
                .hasFieldOrPropertyWithValue("name", "New User");

        assertThat(createdUser.getId()).isNotNull();
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("original@mail.ru");
        user.setLogin("original");
        user.setName("Original User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        User updatedUserData = new User();
        updatedUserData.setId(createdUser.getId());
        updatedUserData.setEmail("updated@mail.ru");
        updatedUserData.setLogin("updated");
        updatedUserData.setName("Updated User");
        updatedUserData.setBirthday(LocalDate.of(1990, 1, 1));

        User updatedUser = userStorage.update(updatedUserData);

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru")
                .hasFieldOrPropertyWithValue("login", "updated")
                .hasFieldOrPropertyWithValue("name", "Updated User");
    }

    @Test
    void testFindUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.findById(9999L));
    }

}
