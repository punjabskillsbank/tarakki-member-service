package com.tarakki.member.util;

import com.tarakki.common.entity.Member;
import com.tarakki.common.enums.AccountStatus;
import com.tarakki.member.dto.MemberDTO;

import java.util.UUID;

public class TestDataFactory {
    public static MemberDTO createMemberDTO() {
        MemberDTO dto = new MemberDTO();
        dto.setFirstName("Sahib");
        dto.setLastName("Singh");
        dto.setEmail("sahib@gmail.com");
        dto.setAccountStatus(AccountStatus.ACTIVE);
        return dto;
    }

    public static Member createMemberEntity() {
        Member member = new Member();
        member.setMemberId(UUID.randomUUID());
        member.setFirstName("Sahib");
        member.setLastName("Singh");
        member.setEmail("sahib@gmail.com");
        member.setAccountStatus(AccountStatus.ACTIVE);
        return member;
    }
}
