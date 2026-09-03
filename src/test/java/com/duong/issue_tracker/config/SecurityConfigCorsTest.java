package com.duong.issue_tracker.config;

import com.duong.issue_tracker.security.JwtAuthenticationFilter;
import com.duong.issue_tracker.security.RestAccessDeniedHandler;
import com.duong.issue_tracker.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigCorsTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Mock
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Test
    void shouldAllowCorsPreflightForFrontendOrigins() {
        SecurityConfig securityConfig = new SecurityConfig(
            jwtAuthenticationFilter,
            userDetailsService,
            restAuthenticationEntryPoint,
            restAccessDeniedHandler);

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "POST");

        CorsConfiguration cors = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).contains("http://localhost:3000", "http://127.0.0.1:3000");
        assertThat(cors.getAllowedMethods()).contains("POST", "OPTIONS");
    }
}
