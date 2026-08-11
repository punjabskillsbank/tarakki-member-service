package com.tarakki.member.Service;

import com.tarakki.member.dto.MemberUpdateDTO;
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
import org.modelmapper.config.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.modelmapper.config.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private UUID memberId;
    private MemberUpdateDTO memberRequestDTO;

    @BeforeEach
    void setUp() {
        dto = MemberTestDataFactory.createMemberDTO();
        member = MemberTestDataFactory.createMemberEntity();
        memberRequestDTO = MemberTestDataFactory.createMemberUpdateDTO();
        memberId = member.getMemberId();
    }

    @Test
    void shouldCreateMember() {
        MemberRequestDTO requestDto = MemberTestDataFactory.createMemberRequestDTO();

        when(modelMapper.map(any(MemberRequestDTO.class), eq(Member.class)))
                .thenReturn(member);

        when(passwordEncoder.encode(anyString()))
                .thenReturn(member.getPasswordHash());

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
                .thenReturn(member.getPasswordHash());
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
                .thenReturn(member.getPasswordHash());
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

    @Test
    void getAllMembers_Success() {

        Member entity = new Member();
        when(memberRepository.findAll()).thenReturn(List.of(entity));
        when(modelMapper.map(any(Member.class), eq(MemberDTO.class))).thenReturn(new MemberDTO());

        List<MemberDTO> result = memberService.getAllMembers();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(memberRepository, times(1)).findAll();
        }

    @Test
    void shouldDeleteMember() {
        when(memberRepository.existsById(member.getMemberId())).thenReturn(true);

        memberService.deleteMember(member.getMemberId());

        verify(memberRepository).existsById(member.getMemberId());
        verify(memberRepository).deleteById(member.getMemberId());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void shouldThrowExceptionWhenDeletingMemberThatDoesNotExist() {
        when(memberRepository.existsById(member.getMemberId())).thenReturn(false);

        MemberNotFoundException exception = assertThrows(MemberNotFoundException.class,
                () -> memberService.deleteMember(member.getMemberId()));

        assertEquals("User not found at id:" + member.getMemberId(), exception.getMessage());
        verify(memberRepository).existsById(member.getMemberId());
        verify(memberRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void shouldUpdateMemberById() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        Configuration mockConfig = mock(Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);

        doAnswer(invocation -> {
            member.setMemberId(memberId);
            member.setFirstName(memberRequestDTO.getFirstName());
            member.setLastName(memberRequestDTO.getLastName());
            member.setEmail(memberRequestDTO.getEmail());
            member.setAccountStatus(memberRequestDTO.getAccountStatus());
            return null;
        }).when(modelMapper).map(memberRequestDTO, member);

        when(modelMapper.map(memberRepository.save(member), MemberDTO.class)).thenReturn(dto);
        MemberDTO result = memberService.updateMemberByMemberId(memberId, memberRequestDTO);

        assertNotNull(result);
        assertEquals(memberRequestDTO.getMemberId(), result.getMemberId());
        assertEquals(memberRequestDTO.getFirstName(), result.getFirstName());
        assertEquals(memberRequestDTO.getLastName(), result.getLastName());
        assertEquals(memberRequestDTO.getEmail(), result.getEmail());
        assertEquals(memberRequestDTO.getAccountStatus(), result.getAccountStatus());
        assertEquals(memberRequestDTO.getProfilePhotoS3Key(), result.getProfilePhotoS3Key());

        verify(memberRepository, times(1)).findById(memberId);
        verify(modelMapper, times(1)).getConfiguration();
        verify(mockConfig, times(1)).setSkipNullEnabled(true);
        verify(modelMapper, times(1)).map(memberRequestDTO, member);
        verify(memberRepository, times(2)).save(member);

    }
}
