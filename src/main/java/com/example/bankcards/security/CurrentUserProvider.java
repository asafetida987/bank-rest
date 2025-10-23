package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Компонент для получения текущего аутентифицированного пользователя из контекста Spring Security.
 * Извлекает объект {@link User} из {@link SecurityContextHolder} через {@link CustomUserDetails}.
 */
@Component
@Slf4j
public class CurrentUserProvider {

    /**
     * Получает текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь {@link User}
     * @throws ClassCastException если Principal в контексте безопасности не является {@link CustomUserDetails}
     * @throws NullPointerException если контекст безопасности или аутентификация отсутствуют
     */
    public User get() {
        User user = ((CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
        log.debug("Получен текущий пользователь id={}, login={}", user.getId(), user.getLogin());

        return user;
    }
}
