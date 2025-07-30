package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {
    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void createValidFilmTest() { // проверка создания валидного фильма
        assertDoesNotThrow(() -> filmController.create(validFilm),
                "Должен создавать фильм с валидными данными без исключений");
    }

    @Test
    void rejectEmptyNameTest() { // проверка пустого названия
        Film film = validFilm;
        film.setName("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при пустом названии");

        assertEquals("Название не может быть пустым", exception.getMessage(),
                "Неверное сообщение об ошибке для пустого названия");
    }

    @Test
    void rejectLongDescriptionTest() { // проверка слишком длинного описания
        Film film = validFilm;
        film.setDescription("a".repeat(201));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при описании > 200 символов");

        assertEquals("Описание не должно превышать 200 символов", exception.getMessage(),
                "Неверное сообщение об ошибке для длинного описания");
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при отсутствии даты релиза");

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage(),
                "Неверное сообщение об ошибке для null даты релиза");
    }

    @Test
    void rejectNegativeDurationTest() { // проверка отрицательной продолжительности
        Film film = validFilm;
        film.setDuration(-1);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при отрицательной продолжительности");

        assertEquals("Продолжительность должна быть положительной", exception.getMessage(),
                "Неверное сообщение об ошибке для отрицательной продолжительности");
    }

    @Test
    void rejectZeroDurationTest() { // проверка нулевой продолжительности
        Film film = validFilm;
        film.setDuration(0);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Должен выбрасывать исключение при нулевой продолжительности");

        assertEquals("Продолжительность должна быть положительной", exception.getMessage(),
                "Неверное сообщение об ошибке для нулевой продолжительности");
    }

    @Test
    void rejectNullRequestTest() { // проверка реакции на null-запрос
        assertThrows(NullPointerException.class,
                () -> filmController.create(null),
                "Должен выбрасывать NullPointerException при null-запросе");
    }

}
