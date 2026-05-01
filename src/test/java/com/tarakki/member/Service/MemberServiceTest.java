package com.tarakki.member.Service;

import com.tarakki.common.entity.Member;
import com.tarakki.member.dto.MemberDTO;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ModelMapper modelMapper;

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

        when(modelMapper.map(any(MemberDTO.class), eq(Member.class)))
                .thenReturn(member);

        when(memberRepository.save(any(Member.class)))
                .thenReturn(member);

        when(modelMapper.map(any(Member.class), eq(MemberDTO.class)))
                .thenReturn(dto);

        MemberDTO result = memberService.createMember(dto);

        assertNotNull(result);
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getLastName(), result.getLastName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getAccountStatus(), result.getAccountStatus());

        verify(modelMapper).map(any(MemberDTO.class), eq(Member.class));
        verify(memberRepository).save(any(Member.class));
        verify(modelMapper).map(any(Member.class), eq(MemberDTO.class));
    }

    @Test
    void shouldGetMemberById() {

        UUID memberId = MemberTestDataFactory.createRandomUUID();

        MemberDTO myDto = MemberTestDataFactory.createGetMemberDTO();
        myDto.setMemberId(memberId);

        Member myMember = MemberTestDataFactory.createMemberEntity();
        myMember.setMemberId(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(myMember));
        when(modelMapper.map(any(Member.class), eq(MemberDTO.class))).thenReturn(myDto);

        MemberDTO result = memberService.getMemberById(memberId);


        assertNotNull(result);
        assertEquals("Amanpreet", result.getFirstName());
    }
}