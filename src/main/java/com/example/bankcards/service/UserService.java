package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public User findUserByLogin(String login) {

        return userRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new WrongParameterException("Неверный логин или пароль"));
    }

    public User saveUser(User user) {
        if (existsUserByLogin(user.getLogin())) {
            throw new WrongParameterException("Пользователь уже существует");
        }

        return userRepository.save(user);
    }

    private boolean existsUserByLogin(String login) {

        return userRepository.existsUserByLogin(login);
    }
}
