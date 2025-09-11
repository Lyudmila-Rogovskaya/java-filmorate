package ru.yandex.practicum.filmorate.storage.bd;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    //private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        insertGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateGenres(film);

        return findById(film.getId());
    }

    private boolean filmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    private boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilmBasic);
        loadGenres(films);
        loadLikes(films);
        return films;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_code"));
        mpa.setDescription(rs.getString("mpa_description"));
        film.setMpa(mpa);

        return film;
    }

    private Film mapRowToFilmBasic(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        film.setMpa(mpa);

        return film;
    }


    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                film.getGenres().add(genre);
            }
        });
    }

    private void loadLikes(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT film_id, user_id FROM likes " +
                "WHERE film_id IN (" + inSql + ")";

        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getLikes().add(rs.getLong("user_id"));
            }
        });
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Genre> genres = new ArrayList<>(film.getGenres());
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genres.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        }
    }

    @Override
    public Film findById(Long id) {
        String sql = "SELECT f.*, m.code AS mpa_code, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            loadGenres(Collections.singletonList(film));
            loadLikes(Collections.singletonList(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    private void insertGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Genre> genres = new ArrayList<>(film.getGenres());

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Genre genre = genres.get(i);
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genre.getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilmBasic, count);
        loadGenres(films);
        loadLikes(films);
        return films;
    }

}
