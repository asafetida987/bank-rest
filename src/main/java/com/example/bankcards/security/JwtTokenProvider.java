package com.example.bankcards.security;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.access_secret}")
    private String accessSecretKey;

    @Value("${jwt.refresh_secret}")
    private String refreshSecretKey;

    @Value("${jwt.access_expiration}")
    private long accessMaxAge;

    private final CustomUserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.getBytes());
        refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey.getBytes());
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessMaxAge);

        return Jwts.builder()
                .claim("login", user.getLogin())
                .issuedAt(now)
                .expiration(validity)
                .signWith(getSigningKey(accessSecretKey))
                .compact();
    }

    public String createRefreshToken(RefreshToken refreshToken) {
        return Jwts.builder()
                .claim("uuid", refreshToken.getToken())
                .expiration(Date.from(refreshToken.getExpiryDate()))
                .signWith(getSigningKey(refreshSecretKey))
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey(token))
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractLogin(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(accessSecretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("login", String.class);
    }

    public String extractUUIDRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(refreshSecretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("uuid", String.class);
    }

    public Authentication getAuthentication(String token) {
        String email = extractLogin(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

}
