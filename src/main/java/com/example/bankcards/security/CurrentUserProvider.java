package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CurrentUserProvider {

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
