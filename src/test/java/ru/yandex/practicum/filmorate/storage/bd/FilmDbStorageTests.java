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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@ActiveProfiles("test")
@Import({MpaDbStorage.class, GenreDbStorage.class, LikeDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTests {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;
    private final UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate, genreStorage, mpaStorage);

        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testFindFilmById() {
        Film newFilm = createTestFilm();
        Film createdFilm = filmStorage.create(newFilm);

        Film foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", createdFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("description", "Test Description");
    }

    @Test
    void testFindAllFilms() {
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Another Film");

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Test Film", "Another Film");
    }

    @Test
    void testCreateFilm() {
        Film newFilm = createTestFilm();

        Film createdFilm = filmStorage.create(newFilm);

        assertThat(createdFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("description", "Test Description");

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getMpa()).isNotNull();
        assertThat(createdFilm.getGenres()).hasSize(2);
    }

    @Test
    void testUpdateFilm() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");

        Set<Genre> newGenres = new HashSet<>();
        newGenres.add(genreStorage.getGenreById(3)); // Мультфильм
        createdFilm.setGenres(newGenres);

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("id", createdFilm.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description");

        assertThat(updatedFilm.getGenres())
                .hasSize(1)
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = createTestFilm();

        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);
        Film createdFilm = filmStorage.create(film);

        likeStorage.addLike(createdFilm.getId(), createdUser.getId());

        Film filmWithLike = filmStorage.findById(createdFilm.getId());
        assertThat(filmWithLike.getLikes()).hasSize(1);

        likeStorage.removeLike(createdFilm.getId(), createdUser.getId());

        Film filmWithoutLike = filmStorage.findById(createdFilm.getId());
        assertThat(filmWithoutLike.getLikes()).isEmpty();
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Another Film");

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

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);

        likeStorage.addLike(createdFilm1.getId(), createdUser1.getId());
        likeStorage.addLike(createdFilm1.getId(), createdUser2.getId());
        likeStorage.addLike(createdFilm2.getId(), createdUser1.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms)
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactly(createdFilm1.getId(), createdFilm2.getId());
    }

    @Test
    void testFindFilmByIdNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.findById(9999L));
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = mpaStorage.getMpaById(1); // G
        film.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        genres.add(genreStorage.getGenreById(1)); // Комедия
        genres.add(genreStorage.getGenreById(2)); // Драма
        film.setGenres(genres);

        return film;
    }

}
