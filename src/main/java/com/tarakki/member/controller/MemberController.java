package com.tarakki.member.controller;


import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody MemberDTO memberDTO) {
        MemberDTO result = memberService.createMember(memberDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);

    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDTO> getMemberDetailsByMemberId(@PathVariable UUID memberId) {
        MemberDTO memberDTO = memberService.getMemberDetailsByMemberId(memberId);
        return ResponseEntity.ok(memberDTO);
    }

    @GetMapping("/{email}")
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable String email) {
        MemberDTO result = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(result);
    }
}