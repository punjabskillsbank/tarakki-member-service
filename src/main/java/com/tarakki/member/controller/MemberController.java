package com.tarakki.member.controller;

import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;
import com.tarakki.member.dto.MemberUpdateDTO;
import com.tarakki.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody MemberRequestDTO memberRequestDTO) {
        MemberDTO result = memberService.createMember(memberRequestDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDTO> getMemberDetailsByMemberId(@PathVariable UUID memberId) {
        MemberDTO memberDTO = memberService.getMemberDetailsByMemberId(memberId);
        return ResponseEntity.ok(memberDTO);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable String email) {
        MemberDTO result = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        List<MemberDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable UUID memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok("Member deleted successfully with id: " + memberId);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberDTO> updateMemberByMemberId(@PathVariable UUID memberId, @Valid @RequestBody MemberUpdateDTO memberRequestDTO) {
        MemberDTO memberDTO = memberService.updateMemberByMemberId(memberId, memberRequestDTO);
        return new ResponseEntity<>(memberDTO, HttpStatus.OK);
    }
}