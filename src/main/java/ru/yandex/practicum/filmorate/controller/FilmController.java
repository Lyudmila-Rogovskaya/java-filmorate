package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController { // для обслуживания фильмов

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @GetMapping
    public List<Film> findAll() { // возвращаем список всех фильмов
        log.info("Запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) { // создаем новый фильм
        log.info("Запрос на создание фильма: {}", film);

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм создан: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) { // обновляем существующий фильм
        log.info("Запрос на обновление фильма: {}", film);

        if (film.getId() == null) {
            log.warn("Id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм не найден");
        }

        Film existingFilm = films.get(film.getId());

        // обновление только валидных полей
        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                log.warn("Название фильма пустое");
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
                log.warn("Описание фильма слишком длинное: {} символов", film.getDescription().length());
                throw new ValidationException("Описание не должно превышать " + MAX_DESCRIPTION_LENGTH + " символов");
            }
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
                log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                log.warn("Некорректная продолжительность: {}", film.getDuration());
                throw new ValidationException("Продолжительность должна быть положительной");
            }
            existingFilm.setDuration(film.getDuration());
        }

        log.info("Фильм обновлен: {}", existingFilm);
        return existingFilm;

    }

    private void validateFilm(Film film) { // // проверяем на корректность: название, описание, дату релиза, продолжительность
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Описание фильма слишком длинное: {} символов", film.getDescription().length());
            throw new ValidationException("Описание не должно превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }

    public void reset() { // очистить (для тестов)
        films.clear();
        nextId = 1;
    }

}
