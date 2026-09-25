package com.tarakki.member.Service;

import com.tarakki.member.entity.Member;
import com.tarakki.member.serviceImpl.JwtServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

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
    void generateToken_shouldProduceTokenWhoseSubjectIsMemberId() {
        String token = jwtService.generateToken(member);

        assertNotNull(token);
        assertEquals(member.getMemberId(), jwtService.extractMemberId(token));
    }

    @Test
    void generateToken_shouldProduceSignedTokenWithHs256Header() {
        String token = jwtService.generateToken(member);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        assertTrue(header.contains("HS256"));
    }

    @Test
    void generateToken_shouldCarryTheMemberNamesAndAnExpiryOneExpirationPeriodAfterIssue() {
        Instant before = Instant.now();

        String token = jwtService.generateToken(member);

        Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        assertEquals(member.getFirstName(), claims.get("firstName", String.class));
        assertEquals(member.getLastName(), claims.get("lastName", String.class));
        Instant lowerBound = before.plusMillis(3600000L).minusSeconds(2);
        Instant upperBound = Instant.now().plusMillis(3600000L).plusSeconds(2);
        assertTrue(claims.getExpiration().toInstant().isAfter(lowerBound));
        assertTrue(claims.getExpiration().toInstant().isBefore(upperBound));
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

    @Test
    void isTokenValid_shouldReturnFalseWhenSubjectIsNotAMemberId() {
        String legacyToken = buildTokenWithSubject("legacy.user@example.com");

        assertFalse(jwtService.isTokenValid(legacyToken));
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenSubjectIsMissing() {
        String tokenWithoutSubject = buildTokenWithSubject(null);

        assertFalse(jwtService.isTokenValid(tokenWithoutSubject));
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String buildTokenWithSubject(String subject) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3600000L))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }
}
