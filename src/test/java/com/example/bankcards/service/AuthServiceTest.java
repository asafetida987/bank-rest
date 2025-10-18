package com.example.bankcards.service;

import com.example.bankcards.dto.request.auth.LoginRequestDTO;
import com.example.bankcards.dto.request.auth.RegisterRequestDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.exception.WrongParameterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnUserResponseDTO(){
        LoginRequestDTO dto = new LoginRequestDTO("test", "testPassword", true);
        User user = User.builder()
                .id(1L)
                .login("test")
                .passwordHash("testPasswordHash")
                .role(UserRole.ROLE_USER)
                .build();

        when(userService.findUserByLogin("test")).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UserResponseDTO responseDTO = authService.login(dto);

        assertEquals(user.getId(), responseDTO.id());
        assertEquals(user.getLogin(), responseDTO.login());
        assertEquals(user.getRole(), responseDTO.role());
        verify(userService, times(1)).findUserByLogin("test");
    }

    @Test
    void login_shouldThrowException_whenPasswordWrong(){
        LoginRequestDTO dto = new LoginRequestDTO("test", "testPassword", true);
        User user = User.builder()
                .id(1L)
                .login("test")
                .passwordHash("testPasswordHash")
                .role(UserRole.ROLE_USER)
                .build();

        when(userService.findUserByLogin("test")).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(WrongParameterException.class, () -> authService.login(dto));
    }

    @Test
    void register_shouldReturnUserResponseDTO(){
        RegisterRequestDTO dto = new RegisterRequestDTO("test", "testPassword", true);
        User user = User.builder()
                .id(1L)
                .login("test")
                .passwordHash("testPasswordHash")
                .role(UserRole.ROLE_USER)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("testPasswordHash");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        UserResponseDTO responseDTO = authService.register(dto);

        assertEquals(user.getId(), responseDTO.id());
        assertEquals(user.getLogin(), responseDTO.login());
        assertEquals(user.getRole(), responseDTO.role());
        verify(userService, times(1)).saveUser(any(User.class));
    }




}
