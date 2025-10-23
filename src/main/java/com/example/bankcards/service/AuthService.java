package com.example.bankcards.service;

import com.example.bankcards.dto.request.auth.LoginRequestDTO;
import com.example.bankcards.dto.request.auth.RegisterRequestDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.exception.WrongParameterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для аутентификации и регистрации пользователей.
 * Отвечает за проверку учетных данных, создание новых учетных записей
 * и возврат DTO, используемых в REST-контроллере.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     *
     * @param dto DTO с учетными данными пользователя
     * @return данные пользователя в виде {@link UserResponseDTO}
     * @throws WrongParameterException если пароль неверный или пользователь не существует
     */
    public UserResponseDTO login(LoginRequestDTO dto) {
        log.info("Попытка входа пользователя login={}", dto.login());
        User user = userService.findUserByLogin(dto.login());
        if (!checkPassword(dto.password(), user.getPasswordHash())) {
            log.warn("Неудачная попытка входа: неверный пароль для login={}", dto.login());
            throw new WrongParameterException("Неверный логин или пароль");
        }
        log.info("Пользователь login={} успешно вошел в систему", dto.login());

        return mapToDTO(user);

    }

    /**
     * Регистрирует нового пользователя в системе.
     * Метод является транзакционным и записывает нового пользователя в базу данных.
     * Пользователь создается с ролью {@code ROLE_USER}.
     *
     * @param dto DTO с регистрационными данными
     * @return данные созданного пользователя
     */
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {
        log.info("Регистрация нового пользователя login={}", dto.login());
        User user = User.builder()
                .login(dto.login())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(UserRole.ROLE_USER)
                .build();
        User saveUser = userService.saveUser(user);
        log.info("Пользователь login={} успешно зарегистрирован с id={}", dto.login(), saveUser.getId());

        return mapToDTO(saveUser);
    }


    private boolean checkPassword(String password, String hash) {

        return passwordEncoder.matches(password, hash);
    }

    private UserResponseDTO mapToDTO(User user) {

        return new UserResponseDTO(user.getId(), user.getLogin(), user.getRole());
    }
}
