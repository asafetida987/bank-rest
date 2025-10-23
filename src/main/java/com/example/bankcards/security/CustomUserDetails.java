package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Обёртка для сущности {@link User}, реализующая интерфейс {@link UserDetails} для Spring Security.
 * Используется для предоставления данных пользователя (логин, пароль, роли) Spring Security
 * во время аутентификации и авторизации.
 */
@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private User user;

    /**
     * Возвращает список полномочий пользователя в формате {@link GrantedAuthority}.
     * Преобразует роль пользователя из UserRole в {@link SimpleGrantedAuthority}.
     *
     * @return коллекция полномочий пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null || user.getRole() == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    /**
     * Возвращает имя пользователя (логин) для аутентификации.
     *
     * @return логин пользователя
     */
    @Override
    public String getUsername() {
        return user.getLogin();
    }

    /**
     * Возвращает хэш пароля пользователя для аутентификации.
     *
     * @return хэш пароля
     */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /**
     * Всегда возвращает {@code true}, так как система не учитывает срок действия аккаунта.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Всегда возвращает {@code true}, так как система не учитывает блокировку аккаунта.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Всегда возвращает {@code true}, так как система не учитывает срок действия учетных данных.
     *
     * @return {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Всегда возвращает {@code true}, так как система не учитывает включение/отключение пользователя.
     *
     * @return {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
