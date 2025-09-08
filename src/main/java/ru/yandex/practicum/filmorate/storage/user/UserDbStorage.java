package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("User not found");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userExists(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userExists(otherId)) {
            throw new NotFoundException("Пользователь с id=" + otherId + " не найден");
        }

        String sql = "SELECT u.* FROM friendships f1 " +
                "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON f1.friend_id = u.user_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    @Override
    public User findById(Long id) {
        if (!userExists(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        String sql = "SELECT * FROM users WHERE user_id = ?";
        User user = jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        loadFriends(user);
        return user;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        jdbcTemplate.query(sql, rs -> {
            Long friendId = rs.getLong("friend_id");
            String status = rs.getString("status");
            user.getFriends().put(
                    friendId,
                    "confirmed".equals(status) ?
                            FriendshipStatus.CONFIRMED :
                            FriendshipStatus.UNCONFIRMED
            );
        }, user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User user) {
        if (!userExists(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public void delete(Long id) {
        if (!userExists(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        String deleteFriendshipsSql = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFriendshipsSql, id, id);

        String deleteLikesSql = "DELETE FROM likes WHERE user_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(deleteUserSql, id);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userExists(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        String sql = "UPDATE friendships SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendId, userId);

        // Добавляем обратную связь для подтвержденной дружбы
        String reverseSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'confirmed')";
        jdbcTemplate.update(reverseSql, userId, friendId);
    }

    private boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

}
