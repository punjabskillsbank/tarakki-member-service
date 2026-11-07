package com.tarakki.member.config;

import com.jayway.jsonpath.JsonPath;
import com.tarakki.member.controller.AuthController;
import com.tarakki.member.controller.MemberController;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.MemberRequestDTO;
import com.tarakki.member.entity.Member;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.JwtService;
import com.tarakki.member.serviceImpl.AuthServiceImpl;
import com.tarakki.member.serviceImpl.JwtServiceImpl;
import com.tarakki.member.serviceImpl.MemberServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Signup returns a JWT. Only the repository is mocked: the real controller, validation, service,
 * ModelMapper, password encoder, JWT service and security filter chain all run, so this checks the
 * actual response body and that the returned token really authenticates the new member.
 */
@WebMvcTest(controllers = {AuthController.class, MemberController.class})
@Import({SecurityConfig.class, ModelMapperConfig.class, JwtServiceImpl.class, AuthServiceImpl.class,
        MemberServiceImpl.class})
@TestPropertySource(properties = {
        "jwt.secret=" + MemberSignupTokenWebMvcTest.SECRET,
        "jwt.expiration-ms=3600000",
        "cors.allowed-origins=http://localhost:5173"
})
class MemberSignupTokenWebMvcTest {

    static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long";
    private static final String OTHER_SECRET = "another-secret-key-that-is-at-least-32-bytes-long";

    // The Boot 4 WebMvc slice does not auto-configure Spring Security, so switch it on explicitly.
    @TestConfiguration
    @EnableWebSecurity
    static class WebSecurityTestConfig {
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MemberRepository memberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private MemberRequestDTO request;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        request = MemberTestDataFactory.createMemberRequestDTO();
        savedMember = null;

        // behave like the database: the saved row gets its own generated id
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setMemberId(UUID.randomUUID());
            savedMember = member;
            return member;
        });
        when(memberRepository.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(savedMember));
        when(memberRepository.findByEmail(request.getEmail())).thenAnswer(invocation -> Optional.ofNullable(savedMember));
    }

    // ---------- the token in the signup response ----------

    @Test
    void shouldReturnFlatMemberFieldsAndTokenWhenSigningUp() throws Exception {
        String body = signUp().andReturn().getResponse().getContentAsString();

        assertEquals(savedMember.getMemberId().toString(), JsonPath.read(body, "$.memberId"));
        assertEquals("Sahib", JsonPath.read(body, "$.firstName"));
        assertEquals("Singh", JsonPath.read(body, "$.lastName"));
        assertEquals("sahib@gmail.com", JsonPath.read(body, "$.email"));
        assertEquals("ACTIVE", JsonPath.read(body, "$.accountStatus"));
        assertEquals("Bearer", JsonPath.read(body, "$.tokenType"));
        assertEquals(3600, (int) JsonPath.read(body, "$.expiresIn"));
        assertNotNull(JsonPath.<String>read(body, "$.token"));
    }

    @Test
    void shouldNotNestTheMemberOrLeakPasswordMaterialInTheSignupResponse() throws Exception {
        signUp()
                .andExpect(jsonPath("$.member").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnTokenWhoseSubjectAndClaimsDescribeTheNewMember() throws Exception {
        String token = tokenFrom(signUp().andReturn().getResponse().getContentAsString());

        Claims claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();

        assertEquals(savedMember.getMemberId().toString(), claims.getSubject());
        assertEquals("Sahib", claims.get("firstName", String.class));
        assertEquals("Singh", claims.get("lastName", String.class));
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(savedMember.getMemberId(), jwtService.extractMemberId(token));
        long lifetimeSeconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertEquals(3600L, lifetimeSeconds);
    }

    @Test
    void shouldNeverPutThePasswordOrItsHashInsideTheToken() throws Exception {
        String token = tokenFrom(signUp().andReturn().getResponse().getContentAsString());

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

        assertFalse(payload.contains(request.getPasswordHash()));
        assertFalse(payload.contains(savedMember.getPasswordHash()));
        assertFalse(payload.toLowerCase().contains("password"));
    }

    @Test
    void shouldAuthenticateTheNewMemberOnProtectedRoutesWithTheSignupToken() throws Exception {
        String token = tokenFrom(signUp().andReturn().getResponse().getContentAsString());
        String memberId = savedMember.getMemberId().toString();

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId));

        mockMvc.perform(get("/api/members/" + memberId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(request.getEmail()));
    }

    @Test
    void shouldStillRejectProtectedRoutesWithoutTheSignupToken() throws Exception {
        signUp();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- the stored account ----------

    @Test
    void shouldStoreOnlyABcryptHashOfThePassword() throws Exception {
        signUp();

        assertNotEquals(request.getPasswordHash(), savedMember.getPasswordHash());
        assertTrue(passwordEncoder.matches(request.getPasswordHash(), savedMember.getPasswordHash()));
    }

    @Test
    void shouldAllowLoggingInWithTheSameCredentialsRightAfterSignup() throws Exception {
        signUp();
        LoginRequestDTO login = MemberTestDataFactory.createLoginRequestDTO();
        login.setPassword(request.getPasswordHash());

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(savedMember.getMemberId(), jwtService.extractMemberId(tokenFrom(body)));
    }

    @Test
    void shouldRejectSignupWithoutTokenWhenAccountStatusIsOmitted() throws Exception {
        // existing behaviour: the request DTO is built with all fields, so the INVITED default is not applied
        String body = "{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"sahib@gmail.com\","
                + "\"passwordHash\":\"securePassword123\"}";

        mockMvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accountStatus").value("must not be null"))
                .andExpect(jsonPath("$.token").doesNotExist());

        verify(memberRepository, never()).save(any(Member.class));
    }

    // ---------- signup stays public whatever the Authorization header says ----------

    @Test
    void shouldSignUpWithAMalformedBearerToken() throws Exception {
        signUpWithAuthorization("Bearer not-a-jwt")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldSignUpWithAnExpiredBearerToken() throws Exception {
        String expired = buildToken(SECRET, UUID.randomUUID().toString(), -60_000L);

        signUpWithAuthorization("Bearer " + expired)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldSignUpWithABearerTokenSignedByAnotherSecret() throws Exception {
        String foreign = buildToken(OTHER_SECRET, UUID.randomUUID().toString(), 60_000L);

        signUpWithAuthorization("Bearer " + foreign)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldIssueTheNewMembersOwnTokenEvenWhenAnotherMembersTokenIsSent() throws Exception {
        String otherMembersToken = buildToken(SECRET, UUID.randomUUID().toString(), 60_000L);

        String body = signUpWithAuthorization("Bearer " + otherMembersToken)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals(savedMember.getMemberId(), jwtService.extractMemberId(tokenFrom(body)));
        assertNotEquals(otherMembersToken, tokenFrom(body));
    }

    // ---------- no account, no token ----------

    @Test
    void shouldReturnConflictAndNoTokenWhenTheEmailAlreadyExists() throws Exception {
        when(memberRepository.save(any(Member.class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Member with email " + request.getEmail() + " already exists."))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void shouldReturnNoTokenWhenTheDuplicateKeyIsNotTheEmail() throws Exception {
        when(memberRepository.save(any(Member.class))).thenThrow(new DuplicateKeyException("other duplicate"));
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // existing behaviour: the service yields no member, so the body is empty and carries no token
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnBadRequestAndSaveNothingWhenTheEmailIsBlank() throws Exception {
        request.setEmail("");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist());

        verify(memberRepository, never()).save(any(Member.class));
    }

    // ---------- helpers ----------

    private ResultActions signUp() throws Exception {
        return mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private ResultActions signUpWithAuthorization(String authorization) throws Exception {
        return mockMvc.perform(post("/api/members")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String tokenFrom(String responseBody) {
        return JsonPath.read(responseBody, "$.token");
    }

    private String buildToken(String secret, String subject, long expiresInMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiresInMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }
}
