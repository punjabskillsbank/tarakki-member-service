package com.tarakki.member.dto;

import com.tarakki.common.dto.MemberDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {

    private String token;

    private String tokenType;

    private long expiresIn;

    private MemberDTO member;
}
