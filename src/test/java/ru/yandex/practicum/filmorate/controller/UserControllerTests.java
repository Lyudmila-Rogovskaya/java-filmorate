package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.bd.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.bd.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Import({UserController.class, UserService.class, UserDbStorage.class, FriendshipDbStorage.class})
class UserControllerTests {
    @Autowired
    private UserController userController;

    @Test
    void createValidUserTest() { // проверка создания валидного пользователя
        User validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));

        assertDoesNotThrow(() -> userController.create(validUser),
                "Должен создавать пользователя с валидными данными без исключений");
    }

    @Test
    void rejectDuplicateEmailTest() { // проверка дублирования email
        User firstUser = new User();
        firstUser.setEmail("duplicate@email.com");
        firstUser.setLogin("first");
        firstUser.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(firstUser);

        User duplicateUser = new User();
        duplicateUser.setEmail("duplicate@email.com");
        duplicateUser.setLogin("second");
        duplicateUser.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(DataIntegrityViolationException.class,
                () -> userController.create(duplicateUser),
                "Должен выбрасывать исключение при дублировании email");
    }

    @Test
    void setLoginAsNameWhenNameEmptyTest() { // проверка установки логина как имени
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

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
