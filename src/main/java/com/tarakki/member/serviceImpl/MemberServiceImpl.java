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

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

    @Override
    public MemberDTO createMember(MemberDTO memberDTO) {
        try {
            Member member = modelMapper.map(memberDTO, Member.class);
            Member savedMember = memberRepository.save(member);
            return modelMapper.map(savedMember, MemberDTO.class);
        } catch (DuplicateKeyException e) {
            if (existsByEmail(memberDTO.getEmail())) {
                throw new MemberEmailAlreadyExistsException(memberDTO.getEmail());
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
}
