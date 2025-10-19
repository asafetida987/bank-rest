package com.example.bankcards.service;

import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserApiServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserApiService userApiService;

    @Test
    void getUserById_shouldReturnUserResponseDTO() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO responseDTO = userApiService.getUserById(1L);

        assertNotNull(responseDTO);
        assertEquals(user.getId(), responseDTO.id());
        assertEquals(user.getLogin(), responseDTO.login());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userApiService.getUserById(1L));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        userApiService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void findAllUsers_shouldReturnPagedResponseDTO() {
        int page = 0;
        int size = 10;
        Pageable expectedPageable = PageRequest.of(page, size);

        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        Page<User> users = new PageImpl<>(List.of(user),  expectedPageable, 1);

        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(expectedPageable))).thenReturn(users);

        PagedResponseDTO<UserResponseDTO> responseDTO = userApiService.findAllUsers(page, size, null, null, null);

        assertEquals(1, responseDTO.totalElements());
        assertEquals("test", responseDTO.content().get(0).login());
        verify(userRepository, times(1)).findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class));
    }

    @Test
    void findAllUsers_shouldUseDefaultPagination_whenPageAndSizeNull() {
        Pageable defaultPageable = PageRequest.of(0, 100);

        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(defaultPageable)))
                .thenReturn(new PageImpl<>(List.of(), defaultPageable, 0));

        userApiService.findAllUsers(0, 100, null, null, null);

        verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any(), eq(defaultPageable));
    }



}
