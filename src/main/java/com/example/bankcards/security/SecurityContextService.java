package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotAuthenticatedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    public boolean isAuthenticated() {

        return checkAuthentication(SecurityContextHolder.getContext().getAuthentication());
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!checkAuthentication(authentication)) {
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }

    private boolean checkAuthentication(Authentication authentication){

        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
