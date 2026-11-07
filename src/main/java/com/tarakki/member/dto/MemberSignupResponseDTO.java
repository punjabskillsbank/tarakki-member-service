package com.tarakki.member.dto;

import com.tarakki.common.dto.MemberDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response of {@code POST /api/members}: the usual flat member fields plus the JWT of the
 * newly created member, so signup does not need a separate login call.
 */
@NoArgsConstructor
@Getter
@Setter
public class MemberSignupResponseDTO extends MemberDTO {

    private String token;

    private String tokenType;

    private long expiresIn;
}
