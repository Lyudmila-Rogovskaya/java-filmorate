package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;

import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.MethodArgumentNotValidException;


@SpringBootTest
class UserControllerTests {
    @Autowired
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        //userController = new UserController();
        userController.reset();
        validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setName("Valid Name");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createValidUserTest() { // проверка создания валидного пользователя
        assertDoesNotThrow(() -> userController.create(validUser),
                "Должен создавать пользователя с валидными данными без исключений");
    }

    @Test
    void rejectEmptyEmailTest() { // проверка пустого email
        User user = validUser;
        user.setEmail("");

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при пустом email");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при пустом email");

        assertEquals("Электронная почта должна содержать @", exception.getMessage(),
                "Неверное сообщение об ошибке для пустого email");
    }

    @Test
    void rejectNullEmailTest() { // проверка отсутствия email
        User user = validUser;
        user.setEmail(null);

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при отсутствии email");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при отсутствии email");

        assertEquals("Электронная почта должна содержать @", exception.getMessage(),
                "Неверное сообщение об ошибке для null email");
    }

    @Test
    void rejectEmailWithoutAtTest() { // проверка email без символа @
        User user = validUser;
        user.setEmail("invalidemail.com");

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при email без @");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при email без @");

        assertEquals("Электронная почта должна содержать @", exception.getMessage(),
                "Неверное сообщение об ошибке для email без @");
    }

    @Test
    void rejectEmptyLoginTest() { // проверка пустого логина
        User user = validUser;
        user.setLogin("");

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при пустом логине");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при пустом логине");

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage(),
                "Неверное сообщение об ошибке для пустого логина");
    }

    @Test
    void rejectNullLoginTest() { // проверка отсутствия логина
        User user = validUser;
        user.setLogin(null);

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при отсутствии логина");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при отсутствии логина");

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage(),
                "Неверное сообщение об ошибке для null логина");
    }

    @Test
    void rejectLoginWithSpaceTest() { // проверка логина с пробелом
        User user = validUser;
        user.setLogin("login with space");

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при логине с пробелом");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при логине с пробелом");

        assertEquals("Логин не может быть пустым или содержать пробелы", exception.getMessage(),
                "Неверное сообщение об ошибке для логина с пробелом");
    }

    @Test
    void rejectFutureBirthdayTest() { // проверка даты рождения в будущем
        User user = validUser;
        user.setBirthday(LocalDate.now().plusDays(1));

//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(user),
//                "Должен выбрасывать исключение при дате рождения в будущем");

        MethodArgumentNotValidException exception = assertThrows(MethodArgumentNotValidException.class,
                () -> userController.create(user),
                "Должен выбрасывать исключение при дате рождения в будущем");

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage(),
                "Неверное сообщение об ошибке для даты рождения в будущем");
    }

    @Test
    void rejectDuplicateEmailTest() { // проверка дублирования email
        userController.create(validUser);
        User duplicateUser = new User();
        duplicateUser.setEmail("valid@email.com");
        duplicateUser.setLogin("anotherLogin");
        duplicateUser.setBirthday(LocalDate.of(2000, 1, 1));

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class,
                () -> userController.create(duplicateUser),
                "Должен выбрасывать исключение при дублировании email");

        assertEquals("Этот имейл уже используется", exception.getMessage(),
                "Неверное сообщение об ошибке для дубликата email");
    }

    @Test
    void setLoginAsNameWhenNameEmptyTest() { // проверка установки логина как имени
        User user = validUser;
        user.setName("");

        User createdUser = userController.create(user);
        assertEquals(user.getLogin(), createdUser.getName(),
                "Должен устанавливать логин как имя при пустом имени");
    }

    @Test
    void rejectNullRequestTest() { // проверка реакции на null-запрос
        assertThrows(NullPointerException.class,
                () -> userController.create(null),
                "Должен выбрасывать NullPointerException при null-запросе");
    }

}
