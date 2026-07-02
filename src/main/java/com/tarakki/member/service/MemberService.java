package com.tarakki.member.service;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;

import java.util.UUID;

public interface MemberService {

    MemberDTO createMember(MemberRequestDTO memberDTO);

    boolean existsByEmail(String email);

    MemberDTO getMemberByEmail(String email);

    MemberDTO getMemberDetailsByMemberId(UUID memberId);
}
