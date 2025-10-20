package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User findUserById(Long userId) {
        log.info("Поиск пользователя по id={}", userId);
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new UserNotFoundException("Пользователь не найден");
                });
        log.info("Пользователь с id={} найден", userId);

        return user;
    }

    public User findUserByLogin(String login) {
        log.info("Поиск пользователя по логину={}", login);
        User user =  userRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> {
                    log.warn("Пользователь с логином={} не найден", login);
                    return new WrongParameterException("Неверный логин или пароль");
                });
        log.info("Пользователь с логином={} найден", login);

        return user;
    }

    public User saveUser(User user) {
        log.info("Сохранение пользователя login={}", user.getLogin());
        if (existsUserByLogin(user.getLogin())) {
            log.warn("Пользователь с логином={} уже существует", user.getLogin());
            throw new WrongParameterException("Пользователь уже существует");
        }
        User saveUser = userRepository.save(user);
        log.info("Пользователь login={} успешно сохранен с id={}", saveUser.getLogin(), saveUser.getId());

        return saveUser;
    }

    private boolean existsUserByLogin(String login) {

        return userRepository.existsUserByLogin(login);
    }
}
