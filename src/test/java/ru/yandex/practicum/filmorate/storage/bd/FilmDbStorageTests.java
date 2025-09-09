package ru.yandex.practicum.filmorate.storage.bd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmDbStorageTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        // Очистка таблиц и добавление тестовых данных
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM genres");

        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (1, 'Комедия')");

        jdbcTemplate.update("INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                        "VALUES (?, ?, ?, ?, ?)",
                "Film 1", "Description 1", Date.valueOf("2000-01-01"), 120, 1);
    }

    @Test
    void testUpdateFilmGenres() {
        Film film = filmStorage.findById(1L);

        // Добавляем жанры
        Set<Genre> genres = new HashSet<>();
        genres.add(genreStorage.getGenreById(1));
        film.setGenres(genres);

        filmStorage.update(film);

        Film updatedFilm = filmStorage.findById(1L);
        assertEquals(1, updatedFilm.getGenres().size(), "Жанры не обновлены");
    }

    @Test
    public void testCreateAndFindFilmById() {
        Film film = createTestFilm("Test Film", "Test Description");
        Film createdFilm = filmStorage.create(film);
        Film foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
        assertThat(foundFilm.getDescription()).isEqualTo("Test Description");
    }

    @Test
    public void testAddLikeAndGetPopular() {
        Film film1 = filmStorage.create(createTestFilm("Film 1", "Desc 1"));
        Film film2 = filmStorage.create(createTestFilm("Film 2", "Desc 2"));

        filmStorage.addLike(film1.getId(), 1L);
        filmStorage.addLike(film1.getId(), 2L);
        filmStorage.addLike(film2.getId(), 1L);

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film1.getId());
    }

    private Film createTestFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpaId(1);
        return film;
    }

}
