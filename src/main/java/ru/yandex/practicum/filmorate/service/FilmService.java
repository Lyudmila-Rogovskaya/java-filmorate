package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existing = filmStorage.findById(film.getId());
        film.setGenres(existing.getGenres());
        film.setMpa(existing.getMpa());
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", filmId, userId);
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным");
        }

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

}
