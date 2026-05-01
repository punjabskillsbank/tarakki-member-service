package com.tarakki.member.serviceImpl;

import com.tarakki.common.entity.Member;
import com.tarakki.member.dto.MemberDTO;
import java.util.UUID;
import com.tarakki.member.repository.MemberRepository;
import com.tarakki.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

    @Override
    public MemberDTO createMember(MemberDTO memberDTO) {
        Member member = modelMapper.map(memberDTO, Member.class);
        Member savedMember = memberRepository.save(member);
        return modelMapper.map(savedMember, MemberDTO.class);
    }

    @Override
    public MemberDTO getMemberById(UUID member_id) {
        Member entity = memberRepository.findById(member_id)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + member_id));

        return modelMapper.map(entity, MemberDTO.class);
    }
}