package com.tarakki.member.service;

import com.tarakki.member.entity.Member;

public interface JwtService {

    String generateToken(Member member);

    String extractEmail(String token);

    boolean isTokenValid(String token);
}
