package com.duong.issue_tracker.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtUtil.generateToken("testuser");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void getUsernameFromToken_shouldExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtUtil.generateToken("testuser");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertThat(jwtUtil.validateToken("invalid-token")).isFalse();
    }

    @Test
    void getUsernameFromToken_shouldThrowException_forInvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.getUsernameFromToken("invalid-token"));
    }
}
