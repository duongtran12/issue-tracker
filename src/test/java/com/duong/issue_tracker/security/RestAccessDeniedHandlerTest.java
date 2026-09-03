package com.duong.issue_tracker.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class RestAccessDeniedHandlerTest {

    @Test
    void handle_shouldReturnJson403() throws Exception {
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler();
        HttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/projects/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getContentAsString()).contains("\"status\":403");
        assertThat(response.getContentAsString()).contains("\"path\":\"/api/projects/1\"");
    }
}
