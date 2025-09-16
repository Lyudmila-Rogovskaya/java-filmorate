package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.bd.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Import({FilmDbStorage.class, UserDbStorage.class,
        MpaDbStorage.class, GenreDbStorage.class,
        LikeDbStorage.class, FilmService.class, FilmController.class})
class FilmControllerTests {
    @Autowired
    private FilmController filmController;

    @Autowired
    private MpaDbStorage mpaStorage;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);

        Mpa mpa = mpaStorage.getMpaById(1); // G рейтинг
        validFilm.setMpa(mpa);
    }

    @Test
    void createValidFilmTest() { // проверка создания валидного фильма
        assertDoesNotThrow(() -> filmController.create(validFilm),
                "Должен создавать фильм с валидными данными без исключений");
    }

    @Test
    void rejectEarlyReleaseDateTest() { // проверка даты релиза до 28.12.1895
        Film film = validFilm;
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при дате релиза до 28.12.1895");

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage(),
                "Неверное сообщение об ошибке для ранней даты релиза");
    }

    @Test
    void rejectNullReleaseDateTest() { // проверка отсутствия даты релиза
        Film film = validFilm;
        film.setReleaseDate(null);

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при отсутствии даты релиза");
    }

    @Test
    void rejectNullRequestTest() { // проверка реакции на null-запрос
        assertThrows(NullPointerException.class,
                () -> filmController.create(null),
                "Должен выбрасывать NullPointerException при null-запросе");
    }

}
