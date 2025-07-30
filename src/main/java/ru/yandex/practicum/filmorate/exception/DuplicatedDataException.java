package ru.yandex.practicum.filmorate.exception;

public class DuplicatedDataException extends RuntimeException { // дублирование уникальных данных
    public DuplicatedDataException(String message) {
        super(message);
    }

}
