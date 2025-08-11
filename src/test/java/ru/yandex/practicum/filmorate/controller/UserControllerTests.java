package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTests {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createValidUserTest() { // проверка создания валидного пользователя
        assertDoesNotThrow(() -> userController.create(validUser),
                "Должен создавать пользователя с валидными данными без исключений");
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
