package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import com.example.bankcards.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void create_shouldReturnRefreshToken() {
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken result = refreshTokenService.create(user);

        assertEquals(user.getId(), result.getUser().getId());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void getUserByToken_shouldReturnUser(){
        User user = User.builder()
                .id(1L)
                .login("test")
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(Instant.now().plusMillis(3600))
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        User result = refreshTokenService.getUserByToken("test");

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getLogin(), result.getLogin());
    }

    @Test
    void getUserByToken_shouldThrowException_whenTokenNotFound(){
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotAuthenticatedException.class, () -> refreshTokenService.getUserByToken("test"));
    }

    @Test
    void getUserByToken_shouldThrowException_whenExpiryDateBeforeNow(){

        RefreshToken refreshToken = RefreshToken.builder()
                .expiryDate(Instant.now().minusMillis(3600))
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        assertThrows(UserNotAuthenticatedException.class, () -> refreshTokenService.getUserByToken("test"));
    }

    @Test
    void delete_shouldDeleteRefreshToken() {
        refreshTokenService.delete("test");

        verify(refreshTokenRepository, times(1)).deleteRefreshTokenByToken("test");
    }

    @Test
    void deleteByExpiration_shouldDeleteRefreshToken() {
        Instant expiration = Instant.now();
        refreshTokenService.deleteByExpiration(expiration);

        verify(refreshTokenRepository, times(1)).deleteRefreshTokenByExpiryDateBefore(expiration);
    }

}
