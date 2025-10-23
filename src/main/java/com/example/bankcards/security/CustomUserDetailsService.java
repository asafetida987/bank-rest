package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя для Spring Security.
 * Реализует интерфейс {@link UserDetailsService} и используется Spring Security
 * для аутентификации пользователя по логину.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по логину и возвращает {@link UserDetails}.
     * Используется Spring Security во время аутентификации. Если пользователь не найден,
     * выбрасывается {@link UsernameNotFoundException}.
     *
     * @param username логин пользователя
     * @return объект {@link UserDetails} с информацией о пользователе
     * @throws UsernameNotFoundException если пользователь с указанным логином не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return  new CustomUserDetails(user);
    }
}
