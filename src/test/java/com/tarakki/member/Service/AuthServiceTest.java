package com.tarakki.member.Service;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.LoginResponseDTO;
import com.tarakki.member.entity.Member;
import com.tarakki.member.exception.InvalidCredentialsException;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.JwtService;
import com.tarakki.member.serviceImpl.AuthServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequestDTO loginRequestDTO;
    private Member member;
    private MemberDTO memberDTO;

    @BeforeEach
    void setUp() {
        loginRequestDTO = MemberTestDataFactory.createLoginRequestDTO();
        member = MemberTestDataFactory.createMemberEntity();
        memberDTO = MemberTestDataFactory.createMemberDTO();
        ReflectionTestUtils.setField(authService, "expirationMs", 3600000L);
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        when(memberRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(member)).thenReturn("mock-jwt-token");
        when(modelMapper.map(member, MemberDTO.class)).thenReturn(memberDTO);

        LoginResponseDTO result = authService.login(loginRequestDTO);

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertEquals(memberDTO.getEmail(), result.getMember().getEmail());

        verify(memberRepository).findByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder).matches(loginRequestDTO.getPassword(), member.getPasswordHash());
        verify(jwtService).generateToken(member);
        verify(modelMapper).map(member, MemberDTO.class);
    }

    @Test
    void shouldThrowInvalidCredentialsWhenEmailNotFound() {
        when(memberRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequestDTO));

        verify(memberRepository).findByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        when(memberRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequestDTO));

        verify(memberRepository).findByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder).matches(loginRequestDTO.getPassword(), member.getPasswordHash());
        verify(jwtService, never()).generateToken(any());
    }
}
