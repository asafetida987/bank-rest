package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.WrongParameterException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findUserById_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getLogin(), result.getLogin());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findUserById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    void findUserByLogin_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(userRepository.findByLoginIgnoreCase("test")).thenReturn(Optional.of(user));

        User result = userService.findUserByLogin("test");

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getLogin(), result.getLogin());
        verify(userRepository, times(1)).findByLoginIgnoreCase("test");
    }

    @Test
    void findUserByLogin_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByLoginIgnoreCase("test")).thenReturn(Optional.empty());

        assertThrows(WrongParameterException.class, () -> userService.findUserByLogin("test"));
    }

    @Test
    void saveUser_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(userRepository.existsUserByLogin("test")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getLogin(), result.getLogin());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveUser_shouldThrowException_whenUserAlreadyExists() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(userRepository.existsUserByLogin("test")).thenReturn(true);

        assertThrows(WrongParameterException.class, () -> userService.saveUser(user));
    }




}
