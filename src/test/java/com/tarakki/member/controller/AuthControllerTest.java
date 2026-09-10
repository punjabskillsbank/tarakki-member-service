package com.tarakki.member.controller;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.LoginResponseDTO;
import com.tarakki.member.exception.InvalidCredentialsException;
import com.tarakki.member.service.AuthService;
import com.tarakki.member.service.MemberService;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MemberService memberService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LoginRequestDTO loginRequestDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        loginRequestDTO = MemberTestDataFactory.createLoginRequestDTO();
        loginResponseDTO = MemberTestDataFactory.createLoginResponseDTO();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponseDTO.getToken()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.member.email").value(loginResponseDTO.getMember().getEmail()));
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        loginRequestDTO.setEmail("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnCurrentMemberWhenAuthenticated() throws Exception {
        MemberDTO memberDTO = MemberTestDataFactory.createMemberDTO();
        when(memberService.getMemberByEmail(memberDTO.getEmail())).thenReturn(memberDTO);

        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(memberDTO.getEmail(), null, List.of());

        mockMvc.perform(get("/api/auth/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(memberDTO.getEmail()));
    }
}
