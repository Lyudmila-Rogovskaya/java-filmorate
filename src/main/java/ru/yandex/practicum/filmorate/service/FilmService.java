package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final LikeStorage likeStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       GenreStorage genreStorage,
                       MpaStorage mpaStorage,
                       LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;

    }

    public Film create(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("MPA must be specified");
        }
        mpaStorage.getMpaById(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Собираем все ID жанров
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            // Получаем все жанры одним запросом
            List<Genre> existingGenres = genreStorage.getGenresByIds(genreIds);

            // Проверяем, что все запрошенные жанры существуют
            if (existingGenres.size() != genreIds.size()) {
                Set<Integer> foundGenreIds = existingGenres.stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet());

                Set<Integer> missingGenreIds = genreIds.stream()
                        .filter(id -> !foundGenreIds.contains(id))
                        .collect(Collectors.toSet());

                throw new NotFoundException("Жанры с id=" + missingGenreIds + " не найдены");
            }
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.getMpaById(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Аналогичная проверка для обновления
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> existingGenres = genreStorage.getGenresByIds(genreIds);

            if (existingGenres.size() != genreIds.size()) {
                Set<Integer> foundGenreIds = existingGenres.stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet());

                Set<Integer> missingGenreIds = genreIds.stream()
                        .filter(id -> !foundGenreIds.contains(id))
                        .collect(Collectors.toSet());

                throw new NotFoundException("Жанры с id=" + missingGenreIds + " не найдены");
            }
        }

        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", filmId, userId);
        likeStorage.removeLike(filmId, userId);
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
