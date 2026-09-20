package com.duong.issue_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAuthenticationException_shouldReturnUnauthorizedJson() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        AuthenticationException exception = new AuthenticationException("Authentication failed") { };
        var response = handler.handleAuthenticationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", 401);
        assertThat(response.getBody()).containsEntry("message", "Authentication failed");
    }
}
