package com.tarakki.member.serviceImpl;

import com.tarakki.member.entity.Member;
import com.tarakki.member.exception.MemberNotFoundException;
import com.tarakki.common.dto.MemberDTO;
<<<<<<< HEAD
import com.tarakki.member.dto.MemberRequestDTO;
=======
>>>>>>> 9cfd56d (TK_54: resolve comments as per reviews)
import com.tarakki.member.exception.MemberEmailAlreadyExistsException;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberDTO createMember(MemberRequestDTO memberRequestDTO) {
        try {
            Member member = modelMapper.map(memberRequestDTO, Member.class);
            member.setPasswordHash(passwordEncoder.encode(memberRequestDTO.getPasswordHash()));
            Member savedMember = memberRepository.save(member);
            return modelMapper.map(savedMember, MemberDTO.class);
        } catch (DuplicateKeyException e) {
            if (existsByEmail(memberRequestDTO.getEmail())) {
                throw new MemberEmailAlreadyExistsException(memberRequestDTO.getEmail());
            }
        }
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public MemberDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
        return modelMapper.map(member, MemberDTO.class);
    }

    @Override
    public MemberDTO getMemberDetailsByMemberId(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        return modelMapper.map(member, MemberDTO.class);
    }

    @Override
    public List<MemberDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> modelMapper.map(member, MemberDTO.class))
                .toList();
    }
}
