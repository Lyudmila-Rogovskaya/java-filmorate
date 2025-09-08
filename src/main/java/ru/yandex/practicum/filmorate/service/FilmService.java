package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        filmStorage.addLike(filmId, userId);
    }

    public Film create(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", filmId, userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Запрос {} самых популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }

}
