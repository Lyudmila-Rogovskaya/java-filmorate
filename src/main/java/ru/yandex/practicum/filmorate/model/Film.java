package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Film { // модель, описывающая фильмы

    private Long id; // идентификатор

    @NotBlank(message = "Название не может быть пустым")
    private String name; // название

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description; // описание

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate; // дата релиза

    @NotNull(message = "Продолжительность должна быть указана")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration; // продолжительность (минуты)

    private final Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new HashSet<>(); // жанры

    private Mpa mpa; // рейтинг MPA

}
