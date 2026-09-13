package com.icsd.movi.controller;

import com.icsd.movi.domain.model.User;
import com.icsd.movi.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUtilsTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsNull_forAnonymousPrincipal() {
        // Edge case: Anonymous user in context. Should return null as there is no real user to resolve.
        setAuthentication(true, "anonymousUser", "anonymousUser");
        AuthUtils authUtils = new AuthUtils(userService);

        User user = authUtils.getCurrentUser();

        assertNull(user);
    }

    @Test
    void getCurrentUser_returnsResolvedUser_forAuthenticatedPrincipal() {
        // Happy path: Valid authenticated user in context. Should return the User entity from service.
        setAuthentication(true, "principal", "kim");
        User expected = User.builder().username("kim").build();
        when(authentication.getName()).thenReturn("kim");
        when(userService.findByUsername("kim")).thenReturn(expected);
        AuthUtils authUtils = new AuthUtils(userService);

        User actual = authUtils.getCurrentUser();

        verify(userService).findByUsername("kim");
        assertEquals(expected, actual);
    }

    @Test
    void requireCurrentUser_throwsWhenNoAuthentication() {
        // Edge case: Empty SecurityContext. Should throw AccessDeniedException as authentication is required.
        SecurityContextHolder.clearContext();
        AuthUtils authUtils = new AuthUtils(userService);

        assertThrows(AccessDeniedException.class, authUtils::requireCurrentUser);
    }

    @Test
    void getCurrentUser_returnsNull_whenAuthenticationIsNull() {
        // Edge case: Null authentication object. Should return null since there is no user context.
        SecurityContextHolder.clearContext();
        AuthUtils authUtils = new AuthUtils(userService);

        User user = authUtils.getCurrentUser();

        assertNull(user);
    }

    @Test
    void getCurrentUser_returnsNull_whenNotAuthenticated() {
        // Edge case: Unauthenticated user object present. Should return null because authentication state is false.
        setAuthentication(false, "user", "username");
        AuthUtils authUtils = new AuthUtils(userService);

        User user = authUtils.getCurrentUser();

        assertNull(user);
    }

    @Test
    void requireCurrentUser_throwsAccessDenied_whenNotAuthenticated() {
        // Edge case: Unauthenticated user object present. Should throw AccessDeniedException.
        setAuthentication(false, "user", "username");
        AuthUtils authUtils = new AuthUtils(userService);

        assertThrows(AccessDeniedException.class, authUtils::requireCurrentUser);
    }

    @Test
    void getCurrentUser_returnsNull_whenUserNotFoundInService() {
        // Edge case: User in context but not in DB. Should return null as the user cannot be resolved.
        setAuthentication(true, "principal", "unknownUser");
        when(authentication.getName()).thenReturn("unknownUser");
        when(userService.findByUsername("unknownUser")).thenReturn(null);
        AuthUtils authUtils = new AuthUtils(userService);

        User user = authUtils.getCurrentUser();

        assertNull(user);
    }

    @Test
    void requireCurrentUser_throwsAccessDenied_whenUserNotFoundInService() {
        // Edge case: User in context but not in DB. Should throw AccessDeniedException.
        setAuthentication(true, "principal", "unknownUser");
        when(authentication.getName()).thenReturn("unknownUser");
        when(userService.findByUsername("unknownUser")).thenReturn(null);
        AuthUtils authUtils = new AuthUtils(userService);

        assertThrows(AccessDeniedException.class, authUtils::requireCurrentUser);
    }

    private void setAuthentication(boolean authenticated, Object principal, String name) {
        org.mockito.Mockito.lenient().when(authentication.isAuthenticated()).thenReturn(authenticated);
        org.mockito.Mockito.lenient().when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
