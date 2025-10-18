package com.example.bankcards.controller;

import com.example.bankcards.dto.response.PagedResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.UserApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserApiService userApiService;

    @Test
    void getUser_shouldReturn200() throws Exception {
        Long userId = 1L;
        UserResponseDTO responseDTO = new UserResponseDTO(userId, "test", UserRole.ROLE_USER);

        when(userApiService.getUserById(anyLong())).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("test"));
    }

    @Test
    void getUser_shouldReturn400_whenWrongRequest() throws Exception {
        Long userId = -1L;

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 1L;

        when(userApiService.getUserById(anyLong())).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUser_shouldReturn500_whenException() throws Exception {
        Long userId = 1L;

        when(userApiService.getUserById(anyLong())).thenThrow(RuntimeException.class);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Пользователь удален успешно"));
    }

    @Test
    void deleteUser_shouldReturn400_whenWrongRequest() throws Exception {
        Long userId = -1L;

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_shouldReturn500_whenException() throws Exception {
        Long userId = 1L;

        doThrow(RuntimeException.class).when(userApiService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllUsers_shouldReturn200() throws Exception {
        PagedResponseDTO<UserResponseDTO> responseDTO = new PagedResponseDTO<>(List.of(), 0, 2, 3);

        when(userApiService.findAllUsers(anyInt(), anyInt(), anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/users/all")
                        .param("page", "0")
                        .param("size", "2")
                        .param("login", "test")
                        .param("createdAtFrom", "2025-01-01")
                        .param("createdAtTo", "2025-02-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void getAllUsers_shouldReturn400_whenWrongRequest() throws Exception {

        mockMvc.perform(get("/api/v1/users/all")
                        .param("page", "0")
                        .param("size", "0")
                        .param("login", "test")
                        .param("createdAtFrom", "2025-01-01")
                        .param("createdAtTo", "2025-02-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_shouldReturn500_whenException() throws Exception {

        when(userApiService.findAllUsers(anyInt(), anyInt(), anyString(), any(LocalDate.class), any(LocalDate.class))).thenThrow(RuntimeException.class);

        mockMvc.perform(get("/api/v1/users/all")
                        .param("page", "0")
                        .param("size", "1")
                        .param("login", "test")
                        .param("createdAtFrom", "2025-01-01")
                        .param("createdAtTo", "2025-02-01"))
                .andExpect(status().isInternalServerError());
    }


}
