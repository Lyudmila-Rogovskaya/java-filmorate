package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class User { // модель, описывающая пользователей


    private Long id; // идентификатор

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать @")
    private String email; // электронная почта

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login; // логин пользователя

    @JsonProperty("name")
    private String name; // имя для отображения

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthday; // дата рождения

    private Map<Long, FriendshipStatus> friends = new HashMap<>();

}
