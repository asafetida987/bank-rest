package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с контекстом безопасности Spring Security.
 * Позволяет проверять, аутентифицирован ли текущий пользователь, и получать объект пользователя {@link User}.
 */
@Service
public class SecurityContextService {

    /**
     * Проверяет, аутентифицирован ли текущий пользователь.
     *
     * @return true, если пользователь аутентифицирован; false в противном случае
     */
    public boolean isAuthenticated() {

        return checkAuthentication(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     *
     * @return объект текущего пользователя {@link User}
     * @throws UserNotAuthenticatedException если пользователь не аутентифицирован
     */
    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!checkAuthentication(authentication)) {
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }

    private boolean checkAuthentication(Authentication authentication){

        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
