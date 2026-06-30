package com.tarakki.member.service;

import com.tarakki.member.dto.MemberDTO;

import java.util.List;
import java.util.UUID;

public interface MemberService {

    MemberDTO createMember(MemberDTO memberDTO);

    boolean existsByEmail(String email);

    MemberDTO getMemberByEmail(String email);

    MemberDTO getMemberDetailsByMemberId(UUID memberId);

    List<MemberDTO> getAllMembers();
}
