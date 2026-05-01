package com.tarakki.member.service;

import com.tarakki.member.dto.MemberDTO;

import java.lang.reflect.Member;
import java.util.UUID;

public interface MemberService {

    MemberDTO createMember(MemberDTO memberDTO);
    MemberDTO getMemberById(java.util.UUID id);
}
