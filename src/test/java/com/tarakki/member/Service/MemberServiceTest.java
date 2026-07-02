package com.tarakki.member.Service;

import com.tarakki.member.entity.Member;
import com.tarakki.member.exception.MemberNotFoundException;
import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;
import com.tarakki.member.exception.MemberEmailAlreadyExistsException;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.serviceImpl.MemberServiceImpl;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MemberDTO dto;
    private Member member;

    @BeforeEach
    void setUp() {
        dto = MemberTestDataFactory.createMemberDTO();
        member = MemberTestDataFactory.createMemberEntity();
    }

    @Test
    void shouldCreateMember() {
        MemberRequestDTO requestDto = MemberTestDataFactory.createMemberRequestDTO();

        when(modelMapper.map(any(MemberRequestDTO.class), eq(Member.class)))
                .thenReturn(member);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword123");

        when(memberRepository.save(any(Member.class)))
                .thenReturn(member);

        when(modelMapper.map(any(Member.class), eq(MemberDTO.class)))
                .thenReturn(dto);

        MemberDTO result = memberService.createMember(requestDto);

        assertNotNull(result);
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getLastName(), result.getLastName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getAccountStatus(), result.getAccountStatus());

        verify(modelMapper).map(any(MemberRequestDTO.class), eq(Member.class));
        verify(memberRepository).save(any(Member.class));
        verify(modelMapper).map(any(Member.class), eq(MemberDTO.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        MemberRequestDTO requestDto = MemberTestDataFactory.createMemberRequestDTO();

        when(modelMapper.map(any(MemberRequestDTO.class), eq(Member.class)))
                .thenReturn(member);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword123");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new DuplicateKeyException("Duplicate key"));
        when(memberRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(MemberEmailAlreadyExistsException.class, () -> memberService.createMember(requestDto));

        verify(memberRepository).save(any(Member.class));
        verify(memberRepository).existsByEmail(anyString());
    }

    @Test
    void shouldReturnNullWhenNotEmailDuplicate() {
        MemberRequestDTO requestDto = MemberTestDataFactory.createMemberRequestDTO();

        when(modelMapper.map(any(MemberRequestDTO.class), eq(Member.class)))
                .thenReturn(member);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword123");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new DuplicateKeyException("Other duplicate key"));
        when(memberRepository.existsByEmail(anyString()))
                .thenReturn(false);

        MemberDTO result = memberService.createMember(requestDto);

        assertNull(result);

        verify(memberRepository).save(any(Member.class));
        verify(memberRepository).existsByEmail(anyString());
    }

    @Test
    void shouldGetMemberByEmail() {
        String email = dto.getEmail();
        when(memberRepository.findByEmail(email)).thenReturn(java.util.Optional.of(member));
        when(modelMapper.map(member, MemberDTO.class)).thenReturn(dto);

        MemberDTO result = memberService.getMemberByEmail(email);

        assertNotNull(result);
        assertEquals(dto.getEmail(), result.getEmail());
        verify(memberRepository).findByEmail(email);
        verify(modelMapper).map(member, MemberDTO.class);
    }

    @Test
    void shouldThrowMemberNotFoundExceptionWhenEmailDoesNotExist() {
        String email = dto.getEmail();
        when(memberRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberByEmail(email));

        verify(memberRepository).findByEmail(email);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void testGetMemberDetailsByMemberId_shouldReturnMemberDetails() {
        when(memberRepository.findById(member.getMemberId())).thenReturn(Optional.of(member));
        when(modelMapper.map((member), MemberDTO.class)).thenReturn(dto);

        MemberDTO result = memberService.getMemberDetailsByMemberId(member.getMemberId());

        assertNotNull(result);
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getLastName(), result.getLastName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getAccountStatus(), result.getAccountStatus());

        verify(memberRepository).findById(member.getMemberId());
        verify(modelMapper).map((member), MemberDTO.class);

    }

    @Test
    void shouldThrowExceptionWhenMemberNotFound() {
        MemberNotFoundException memberNotFoundException = assertThrows(MemberNotFoundException.class,
                () -> memberService.getMemberDetailsByMemberId(member.getMemberId()));
        assertEquals("User not found at id:" + member.getMemberId(), memberNotFoundException.getMessage());
    }
}