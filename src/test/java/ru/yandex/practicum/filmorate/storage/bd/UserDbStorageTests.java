package ru.yandex.practicum.filmorate.storage.bd;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTests {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
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

        createdUser.setEmail("updated@mail.ru");
        createdUser.setLogin("updated");
        createdUser.setName("Updated User");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", "updated@mail.ru")
                .hasFieldOrPropertyWithValue("login", "updated")
                .hasFieldOrPropertyWithValue("name", "Updated User");
    }

    @Test
    void testAddAndGetFriends() {
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

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = userStorage.getFriends(createdUser1.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(createdUser2.getId());
    }

    @Test
    void testRemoveFriend() {
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

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = userStorage.getFriends(createdUser1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
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

        User user3 = new User();
        user3.setEmail("user3@mail.ru");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);

        userStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        userStorage.addFriend(createdUser2.getId(), createdUser3.getId());
        List<User> commonFriends = userStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(createdUser3.getId());
    }

    @Test
    void testFindUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.findById(9999L));
    }

}
