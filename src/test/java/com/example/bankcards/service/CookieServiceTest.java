package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CookieServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserService userService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CookieService cookieService;

    private static final int accessMaxAge = 3600;
    private static final int refreshMaxAge = 10000;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(cookieService, "accessMaxAge", accessMaxAge);
        ReflectionTestUtils.setField(cookieService, "refreshMaxAge", refreshMaxAge);
    }

    @Test
    void addAuthCookies_shouldAddAuthCookies() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();
        String accessToken = "accessToken";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(11L)
                .token("refreshToken")
                .user(user)
                .build();

        when(userService.findUserById(1L)).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn(accessToken);
        when(refreshTokenService.create(user)).thenReturn(refreshToken);
        when(jwtTokenProvider.createRefreshToken(refreshToken)).thenReturn("refreshTokenWithSign");

        ArgumentCaptor<String> cookieArgumentCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.addAuthCookies(response, 1L, true);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieArgumentCaptor.capture());
        List<String> cookies = cookieArgumentCaptor.getAllValues();
        assertEquals(2, cookies.size());
        String accessHeader = cookies.get(0);
        String refreshHeader = cookies.get(1);

        assertTrue(accessHeader.contains("access_token=accessToken"));
        assertTrue(refreshHeader.contains("refresh_token=refreshTokenWithSign"));
        assertTrue(accessHeader.contains("HttpOnly"));
        assertTrue(refreshHeader.contains("HttpOnly"));
        assertTrue(accessHeader.contains("Max-Age=3600"));
        assertTrue(refreshHeader.contains("Max-Age=10000"));
    }

    @Test
    void addAuthCookies_shouldNotAddRefreshCookies_whenRememberMeFalse() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();
        String accessToken = "accessToken";

        when(userService.findUserById(1L)).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn(accessToken);

        ArgumentCaptor<String> cookieArgumentCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.addAuthCookies(response, 1L, false);

        verify(response, times(1)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieArgumentCaptor.capture());
        List<String> cookies = cookieArgumentCaptor.getAllValues();
        assertEquals(1, cookies.size());
        String accessHeader = cookies.get(0);

        assertTrue(accessHeader.contains("access_token=accessToken"));
        assertTrue(accessHeader.contains("HttpOnly"));
        assertTrue(accessHeader.contains("Max-Age=3600"));
    }

    @Test
    void addAuthCookies_shouldThrowException_whenUserNotFound() {
        when(userService.findUserById(1L)).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> cookieService.addAuthCookies(response, 1L, false));

    }

    @Test
    void deleteAuthCookies_shouldDeleteAuthCookies() {
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("refresh_token", "refreshTokenWithSign");

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.extractUUIDRefreshToken("refreshTokenWithSign")).thenReturn("refreshToken");

        ArgumentCaptor<String> cookieArgumentCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.deleteAuthCookies(request, response);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieArgumentCaptor.capture());
        List<String> cookiesResponse = cookieArgumentCaptor.getAllValues();
        assertEquals(2, cookiesResponse.size());
        String accessHeader = cookiesResponse.get(0);
        String refreshHeader = cookiesResponse.get(1);

        assertTrue(accessHeader.contains("access_token="));
        assertTrue(refreshHeader.contains("refresh_token="));
        assertTrue(accessHeader.contains("HttpOnly"));
        assertTrue(refreshHeader.contains("HttpOnly"));
        assertTrue(accessHeader.contains("Max-Age=0"));
        assertTrue(refreshHeader.contains("Max-Age=0"));
    }

    @Test
    void deleteAuthCookies_shouldThrowException_whenCookieNotFound() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () -> cookieService.deleteAuthCookies(request, response));
    }

    @Test
    void refreshAuthCookies_shouldRefreshAccessToken() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie("access_token", "accessTokenBefore");
        cookies[1] = new Cookie("refresh_token", "refreshTokenWithSign");

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.extractUUIDRefreshToken("refreshTokenWithSign")).thenReturn("refreshToken");
        when(refreshTokenService.getUserByToken("refreshToken")).thenReturn(user);
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("accessTokenAfter");

        ArgumentCaptor<String> cookieArgumentCaptor = ArgumentCaptor.forClass(String.class);

        cookieService.refreshAuthCookies(request, response);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieArgumentCaptor.capture());
        List<String> cookiesResponse = cookieArgumentCaptor.getAllValues();
        assertEquals(2, cookiesResponse.size());
        String accessHeader = cookiesResponse.get(0);
        String refreshHeader = cookiesResponse.get(1);

        assertTrue(accessHeader.contains("access_token=accessTokenAfter"));
        assertTrue(refreshHeader.contains("refresh_token=refreshToken"));
        assertTrue(accessHeader.contains("HttpOnly"));
        assertTrue(refreshHeader.contains("HttpOnly"));
        assertTrue(accessHeader.contains("Max-Age=" + accessMaxAge));
        assertTrue(refreshHeader.contains("Max-Age=" + refreshMaxAge));
    }

    @Test
    void refreshAuthCookies_shouldThrowException_whenCookieNotFound() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () -> cookieService.refreshAuthCookies(request, response));
    }

    @Test
    void refreshAuthCookies_shouldThrowException_whenRefreshTokenNotFound() {
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie("access_token", "accessTokenBefore");

        when(request.getCookies()).thenReturn(cookies);

        assertThrows(UserNotAuthenticatedException.class, () -> cookieService.refreshAuthCookies(request, response));
    }




}
