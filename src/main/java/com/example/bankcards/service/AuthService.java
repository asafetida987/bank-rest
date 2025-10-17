package com.example.bankcards.service;

import com.example.bankcards.dto.request.auth.LoginRequestDTO;
import com.example.bankcards.dto.request.auth.RegisterRequestDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.exception.WrongParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO login(LoginRequestDTO dto) {
        User user = userService.findUserByLogin(dto.login());
        if (!checkPassword(dto.password(), user.getPasswordHash())) {
            throw new WrongParameterException("Неверный логин или пароль");
        }

        return mapToDTO(user);

    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {
        User user = User.builder()
                .login(dto.login())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(UserRole.ROLE_USER)
                .build();
        User saveUser = userService.saveUser(user);

        return mapToDTO(saveUser);
    }


    private boolean checkPassword(String password, String hash) {

        return passwordEncoder.matches(password, hash);
    }

    private UserResponseDTO mapToDTO(User user) {

        return new UserResponseDTO(user.getId(), user.getLogin(), user.getRole());
    }
}
