package com.tarakki.member.Service;

import com.tarakki.member.entity.Member;
import com.tarakki.member.serviceImpl.JwtServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long";

    private JwtServiceImpl jwtService;
    private Member member;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
        ReflectionTestUtils.invokeMethod(jwtService, "init");

        member = MemberTestDataFactory.createMemberEntity();
    }

    @Test
    void generateToken_shouldProduceTokenWhoseSubjectIsMemberEmail() {
        String token = jwtService.generateToken(member);

        assertNotNull(token);
        assertEquals(member.getEmail(), jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForFreshlyGeneratedToken() {
        String token = jwtService.generateToken(member);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenSignatureIsTampered() {
        String token = jwtService.generateToken(member);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken(member);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForGarbageInput() {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }
}
