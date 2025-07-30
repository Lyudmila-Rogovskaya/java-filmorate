package ru.yandex.practicum.filmorate.exception;

public class NotFoundException extends RuntimeException { // объект не найден
    public NotFoundException(String message) {
        super(message);
    }

}
