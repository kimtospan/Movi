package com.icsd.movi.controller;

import com.icsd.movi.domain.model.User;
import com.icsd.movi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserService userService;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userService.findByUsername(authentication.getName());
    }

    public User requireCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return user;
    }
}

