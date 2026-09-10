package com.tarakki.member.serviceImpl;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.LoginResponseDTO;
import com.tarakki.member.entity.Member;
import com.tarakki.member.exception.InvalidCredentialsException;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.AuthService;
import com.tarakki.member.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Member member = memberRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(member);
        MemberDTO memberDTO = modelMapper.map(member, MemberDTO.class);

        return new LoginResponseDTO(token, "Bearer", expirationMs / 1000, memberDTO);
    }
}
