package com.tarakki.member.controller;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.LoginResponseDTO;
import com.tarakki.member.service.AuthService;
import com.tarakki.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO result = authService.login(loginRequestDTO);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<MemberDTO> getCurrentMember(Authentication authentication) {
        MemberDTO result = memberService.getMemberByEmail(authentication.getName());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
