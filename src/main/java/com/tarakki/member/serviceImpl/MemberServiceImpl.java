package com.tarakki.member.serviceImpl;

import com.tarakki.common.entity.Member;
import com.tarakki.common.exceptionHandling.MemberNotFoundException;
import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.exception.MemberEmailAlreadyExistsException;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

    @Override
    public MemberDTO createMember(MemberDTO memberDTO) {
        if (existsByEmail(memberDTO.getEmail())) {
            throw new MemberEmailAlreadyExistsException(memberDTO.getEmail());
        }

        try {
            Member member = modelMapper.map(memberDTO, Member.class);
            Member savedMember = memberRepository.save(member);
            return modelMapper.map(savedMember, MemberDTO.class);
        } catch (DuplicateKeyException e) {
            throw new MemberEmailAlreadyExistsException(memberDTO.getEmail());
        }
    }


    @Override
    public MemberDTO getMemberById(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        return modelMapper.map(member, MemberDTO.class);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}