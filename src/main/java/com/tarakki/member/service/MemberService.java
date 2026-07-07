package com.tarakki.member.service;

import com.tarakki.common.dto.MemberDTO;
<<<<<<< HEAD
import com.tarakki.member.dto.MemberRequestDTO;
=======
import com.tarakki.member.entity.Member;
>>>>>>> 9cfd56d (TK_54: resolve comments as per reviews)

import java.util.List;
import java.util.UUID;

public interface MemberService {

    MemberDTO createMember(MemberRequestDTO memberRequestDTO);

    boolean existsByEmail(String email);

    MemberDTO getMemberByEmail(String email);

    MemberDTO getMemberDetailsByMemberId(UUID memberId);

    List<MemberDTO> getAllMembers();
}
