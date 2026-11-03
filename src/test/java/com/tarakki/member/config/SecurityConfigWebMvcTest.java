package com.tarakki.member.config;

import com.jayway.jsonpath.JsonPath;
import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.controller.AuthController;
import com.tarakki.member.controller.MemberController;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.MemberRequestDTO;
import com.tarakki.member.entity.Member;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.JwtService;
import com.tarakki.member.service.MemberService;
import com.tarakki.member.serviceImpl.AuthServiceImpl;
import com.tarakki.member.serviceImpl.JwtServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, MemberController.class})
@Import({SecurityConfig.class, JwtServiceImpl.class, AuthServiceImpl.class})
@TestPropertySource(properties = {
        "jwt.secret=" + SecurityConfigWebMvcTest.SECRET,
        "jwt.expiration-ms=3600000",
        "cors.allowed-origins=" + SecurityConfigWebMvcTest.ALLOWED_ORIGIN
})
class SecurityConfigWebMvcTest {

    static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long";
    static final String OTHER_SECRET = "another-secret-key-that-is-at-least-32-bytes-long";
    static final String ALLOWED_ORIGIN = "http://localhost:5173";

    private static final String UNAUTHENTICATED_BODY = "Authentication required";

    // The Boot 4 WebMvc slice does not auto-configure Spring Security, so switch it on and
    // put the real filter chain in front of MockMvc explicitly.
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
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private Member member;
    private MemberDTO memberDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        member = MemberTestDataFactory.createMemberEntity();
        member.setPasswordHash(passwordEncoder.encode(MemberTestDataFactory.createLoginRequestDTO().getPassword()));
        memberDTO = MemberTestDataFactory.createMemberDTO();
        memberDTO.setMemberId(member.getMemberId());

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(modelMapper.map(member, MemberDTO.class)).thenReturn(memberDTO);
        when(memberService.getAllMembers()).thenReturn(List.of(memberDTO));
        when(memberService.getMemberDetailsByMemberId(member.getMemberId())).thenReturn(memberDTO);
    }

    // ---------- no token: every protected route answers 401 and never reaches the service ----------

    @Test
    void shouldRejectGetAllMembersWithoutToken() throws Exception {
        assertRejected(get("/api/members"));
    }

    @Test
    void shouldRejectGetMemberByIdWithoutToken() throws Exception {
        assertRejected(get("/api/members/" + member.getMemberId()));
    }

    @Test
    void shouldRejectGetMemberByEmailWithoutToken() throws Exception {
        assertRejected(get("/api/members/email/" + member.getEmail()));
    }

    @Test
    void shouldRejectUpdateMemberWithoutToken() throws Exception {
        assertRejected(patch("/api/members/" + member.getMemberId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }

    @Test
    void shouldRejectDeleteMemberWithoutToken() throws Exception {
        assertRejected(delete("/api/members/" + member.getMemberId()));
    }

    @Test
    void shouldRejectCurrentMemberWithoutToken() throws Exception {
        assertRejected(get("/api/auth/me"));
    }

    @Test
    void shouldRejectUnknownPathWithoutToken() throws Exception {
        assertRejected(get("/api/does-not-exist"));
    }

    // ---------- a valid token is accepted ----------

    @Test
    void shouldAllowProtectedRoutesWithValidToken() throws Exception {
        String token = jwtService.generateToken(member);

        mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(memberDTO.getEmail()));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(member.getMemberId().toString()));

        verify(memberService).getAllMembers();
        verify(memberService).getMemberDetailsByMemberId(member.getMemberId());
    }

    @Test
    void shouldAcceptTheTokenReturnedByLogin() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(memberDTO.getEmail()));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(member.getMemberId().toString()));
    }

    @Test
    void shouldReturnNotFoundForUnknownPathWithValidToken() throws Exception {
        String token = jwtService.generateToken(member);

        mockMvc.perform(get("/api/does-not-exist").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    // ---------- bad tokens are all rejected ----------

    @Test
    void shouldRejectMalformedToken() throws Exception {
        assertRejected(get("/api/members").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"));
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        String expired = buildToken(SECRET, member.getMemberId().toString(), -60_000L);

        assertRejected(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(expired)));
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() throws Exception {
        String foreign = buildToken(OTHER_SECRET, member.getMemberId().toString(), 60_000L);

        assertRejected(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(foreign)));
    }

    @Test
    void shouldRejectTokenWhosePayloadWasTamperedWith() throws Exception {
        String[] real = jwtService.generateToken(member).split("\\.");
        Member otherMember = MemberTestDataFactory.createMemberEntity();
        String[] forged = jwtService.generateToken(otherMember).split("\\.");
        String tampered = real[0] + "." + forged[1] + "." + real[2];

        assertRejected(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(tampered)));
    }

    @Test
    void shouldRejectTokenWhoseSubjectIsAnEmailAddress() throws Exception {
        String legacy = buildToken(SECRET, "legacy.user@example.com", 60_000L);

        assertRejected(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(legacy)));
    }

    // ---------- public routes ----------

    @Test
    void shouldLoginWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MemberTestDataFactory.createLoginRequestDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldSignUpWithoutToken() throws Exception {
        MemberRequestDTO request = MemberTestDataFactory.createMemberRequestDTO();
        when(memberService.createMember(any(MemberRequestDTO.class))).thenReturn(memberDTO);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(memberDTO.getEmail()));

        verify(memberService).createMember(any(MemberRequestDTO.class));
    }

    @Test
    void shouldAnswerCorsPreflightWithoutToken() throws Exception {
        mockMvc.perform(options("/api/members")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));

        verifyNoInteractions(memberService);
    }

    @Test
    void shouldNotRequireTokenForApiDocs() throws Exception {
        // springdoc is not part of the WebMvc slice, so a permitted path falls through to 404, not 401
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(memberService);
    }

    // ---------- helpers ----------

    // The rejected request must answer 401 with the plain-text message and never reach the service.
    private void assertRejected(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(UNAUTHENTICATED_BODY));

        verifyNoInteractions(memberService);
    }

    private String loginAndGetToken() throws Exception {
        LoginRequestDTO login = MemberTestDataFactory.createLoginRequestDTO();
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
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
