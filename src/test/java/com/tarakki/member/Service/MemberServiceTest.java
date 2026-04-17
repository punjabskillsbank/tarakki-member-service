package com.tarakki.member.Service;

import com.tarakki.common.entity.Member;
import com.tarakki.common.enums.AccountStatus;
import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.serviceImpl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        dto = new MemberDTO();
        dto.setFirstName("Sahib");
        dto.setLastName("Singh");
        dto.setEmail("sahib@gmail.com");
        dto.setAccountStatus(AccountStatus.ACTIVE);

        member = new Member();
        member.setMemberId(UUID.randomUUID());
        member.setFirstName("Sahib");
        member.setLastName("Singh");
        member.setEmail("sahib@gmail.com");
        member.setAccountStatus(AccountStatus.ACTIVE);
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
        assertEquals("Sahib", result.getFirstName());
        assertEquals("Singh", result.getLastName());
        assertEquals("sahib@gmail.com", result.getEmail());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());

        verify(modelMapper).map(any(MemberDTO.class), eq(Member.class));
        verify(memberRepository).save(any(Member.class));
        verify(modelMapper).map(any(Member.class), eq(MemberDTO.class));
    }
}