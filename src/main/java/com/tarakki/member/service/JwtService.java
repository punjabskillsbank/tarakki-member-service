package com.tarakki.member.service;

import com.tarakki.member.entity.Member;

import java.util.UUID;

public interface JwtService {

    String generateToken(Member member);

    UUID extractMemberId(String token);

    boolean isTokenValid(String token);
}
