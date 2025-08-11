package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTests {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void emailIsBlankTest() { // проверка пустого email
        User user = validUser;
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при пустом email");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("пустой")),
                "Сообщение об ошибке должно указывать на пустой email");
    }

    @Test
    void emailWithoutSymbolTest() { // проверка email без @
        User user = validUser;
        user.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при email без @");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("содержать @")),
                "Сообщение об ошибке должно требовать символ @ в email");
    }

    @Test
    void loginIsBlankTest() { // проверка пустого логина
        User user = validUser;
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при пустом логине");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("пустым")),
                "Сообщение об ошибке должно указывать на пустой логин");
    }

    @Test
    void loginHasSpacesTest() { // проверка логина с пробелами
        User user = validUser;
        user.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при логине с пробелами");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("пробелы")),
                "Сообщение об ошибке должно запрещать пробелы в логине");
    }

    @Test
    void birthdayInFutureTest() { // проверка даты рождения в будущем
        User user = validUser;
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должны быть ошибки валидации при дате рождения в будущем");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("будущем")),
                "Сообщение об ошибке должно запрещать дату рождения в будущем");
    }

}
