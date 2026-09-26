package com.duong.issue_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAuthenticationException_shouldReturnUnauthorizedJson() {
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        AuthenticationException exception = new AuthenticationException("Authentication failed") { };
        var response = handler.handleAuthenticationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Authentication failed");
        assertThat(response.getBody().path()).isEqualTo("/api/auth/login");
    }
}
