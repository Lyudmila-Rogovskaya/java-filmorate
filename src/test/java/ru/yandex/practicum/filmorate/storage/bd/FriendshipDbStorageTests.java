package ru.yandex.practicum.filmorate.storage.bd;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FriendshipDbStorageTests {
    private final JdbcTemplate jdbcTemplate;
    private FriendshipDbStorage friendshipStorage;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        friendshipStorage = new FriendshipDbStorage(jdbcTemplate, new UserDbStorage(jdbcTemplate));
        userStorage = new UserDbStorage(jdbcTemplate);

        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
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

        friendshipStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = friendshipStorage.getFriends(createdUser1.getId());

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

        friendshipStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        friendshipStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = friendshipStorage.getFriends(createdUser1.getId());
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

        friendshipStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        friendshipStorage.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = friendshipStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(createdUser3.getId());
    }

}
