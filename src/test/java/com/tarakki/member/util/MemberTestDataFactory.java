package com.tarakki.member.util;

import com.tarakki.member.entity.Member;
import com.tarakki.common.enums.AccountStatus;
import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;

import java.util.UUID;

public class MemberTestDataFactory {
    public static MemberDTO createMemberDTO() {
        MemberDTO dto = new MemberDTO();
        dto.setFirstName("Sahib");
        dto.setLastName("Singh");
        dto.setEmail("sahib@gmail.com");
        dto.setAccountStatus(AccountStatus.ACTIVE);
        return dto;
    }

    public static MemberRequestDTO createMemberRequestDTO() {
        MemberRequestDTO dto = new MemberRequestDTO();
        dto.setFirstName("Sahib");
        dto.setLastName("Singh");
        dto.setEmail("sahib@gmail.com");
        dto.setPassword("securePassword123");
        dto.setAccountStatus(AccountStatus.ACTIVE);
        return dto;
    }

    public static Member createMemberEntity() {
        Member member = new Member();
        member.setMemberId(UUID.randomUUID());
        member.setFirstName("Sahib");
        member.setLastName("Singh");
        member.setEmail("sahib@gmail.com");
        member.setPassword("encodedPassword123");
        member.setAccountStatus(AccountStatus.ACTIVE);
        return member;
    }
}
