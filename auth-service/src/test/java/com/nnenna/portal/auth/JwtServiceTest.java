package com.nnenna.portal.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234567890";

    @Test
    void issuedTokenCarriesSubjectAndRole() {
        JwtService jwt = new JwtService(SECRET, 60);
        Claims claims = jwt.parse(jwt.issue(7L, "ADMIN"));
        assertEquals("7", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String token = new JwtService("another-secret-another-secret-another-1234", 60).issue(1L, "CUSTOMER");
        JwtService jwt = new JwtService(SECRET, 60);
        assertThrows(JwtException.class, () -> jwt.parse(token));
    }
}
