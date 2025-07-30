package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends RuntimeException { // нарушение бизнес-правил
    public ValidationException(String message) {
        super(message);
    }

}
