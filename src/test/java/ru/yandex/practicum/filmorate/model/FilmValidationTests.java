package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmValidationTests {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void nameIsBlankTest() { // проверка пустого названия
        Film film = validFilm;
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при пустом названии");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Название не может быть пустым")),
                "Сообщение об ошибке должно содержать текст о пустом названии");
    }

    @Test
    void descriptionLengthMax200Test() { // проверка длины описания
        Film film = validFilm;
        String longDescription = "a".repeat(201);
        film.setDescription(longDescription);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при описании >200 символов");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("не должно превышать 200")),
                "Сообщение об ошибке должно указывать на превышение длины описания");
    }

    @Test
    void durationIsNegativeTest() { // проверка отрицательной продолжительности
        validFilm.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Продолжительность должна быть положительным числом")));
    }

    @Test
    void durationIsZeroTest() { // проверка нулевой продолжительности
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Продолжительность должна быть положительным числом")));
    }

    @Test
    void releaseDateIsNullTest() { // проверка отсутствия даты релиза
        Film film = validFilm;
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при отсутствии даты релиза");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("обязательна")),
                "Сообщение об ошибке должно указывать на обязательность даты релиза");
    }

}
