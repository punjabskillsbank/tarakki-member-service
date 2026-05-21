package com.tarakki.member.util;

import com.tarakki.common.entity.Member;
import com.tarakki.common.enums.AccountStatus;
import com.tarakki.member.dto.MemberDTO;

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

    public static Member createMemberEntity() {
        Member member = new Member();
        member.setMemberId(UUID.randomUUID());
        member.setFirstName("Sahib");
        member.setLastName("Singh");
        member.setEmail("sahib@gmail.com");
        member.setAccountStatus(AccountStatus.ACTIVE);
        return member;
    }

    public static MemberDTO createGetMemberDTO() {
        MemberDTO dto = new MemberDTO();
        dto.setFirstName("Amanpreet");
        dto.setLastName("Kaur");
        dto.setEmail("aman@test.com");
        dto.setAccountStatus(AccountStatus.ACTIVE);
        return dto;
    }
    public static UUID createRandomUUID() {
        return UUID.randomUUID();
    }
}
