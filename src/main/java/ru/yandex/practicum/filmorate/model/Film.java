package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Getter
@Setter
public class Film { // модель, описывающая фильмы

    private Long id; // идентификатор

    @NotBlank(message = "Название не может быть пустым")
    private String name; // название

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description; // описание

    private LocalDate releaseDate; // дата релиза

    @NotNull(message = "Продолжительность должна быть указана")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration; // продолжительность (минуты)

}
