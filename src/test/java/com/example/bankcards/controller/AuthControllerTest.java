package com.example.bankcards.controller;

import com.example.bankcards.dto.request.auth.LoginRequestDTO;
import com.example.bankcards.dto.request.auth.RegisterRequestDTO;
import com.example.bankcards.dto.response.MessageResponseDTO;
import com.example.bankcards.dto.response.user.UserResponseDTO;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private CookieService cookieService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_shouldReturn200() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("test", "password", true);
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "test", UserRole.ROLE_USER);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("test"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
        verify(cookieService, times(1)).addAuthCookies(any(HttpServletResponse.class), anyLong(), anyBoolean());
    }

    @Test
    void login_shouldReturn400_whenWrongRequest() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO(" ", " ", true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn500_whenException() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("test", "password", true);

        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void register_shouldReturn200() throws Exception {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO("test", "Password1", true);
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "test", UserRole.ROLE_USER);

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("test"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
        verify(cookieService, times(1)).addAuthCookies(any(HttpServletResponse.class), anyLong(), anyBoolean());
    }

    @Test
    void register_shouldReturn400_whenWrongRequest() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("test", "Password", true);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn500_whenException() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO("test", "Password1", true);

        when(authService.register(any(RegisterRequestDTO.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void logout_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Выход осуществлен успешно"));

        verify(cookieService, times(1)).deleteAuthCookies(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void logout_shouldReturn500_whenException() throws Exception {
        doThrow(RuntimeException.class).when(cookieService).deleteAuthCookies(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void refresh_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Токен обновлен успешно"));

        verify(cookieService, times(1)).refreshAuthCookies(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void refresh_shouldReturn500_whenException() throws Exception {
        doThrow(RuntimeException.class).when(cookieService).refreshAuthCookies(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isInternalServerError());
    }


}
