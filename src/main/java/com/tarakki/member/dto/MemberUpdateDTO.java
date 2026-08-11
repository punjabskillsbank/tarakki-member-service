package com.tarakki.member.dto;

import com.tarakki.common.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberUpdateDTO {
    private UUID memberId;

    private String firstName;

    private String lastName;

    private String email;

    private String passwordHash;

    private String profilePhotoS3Key;

    private AccountStatus accountStatus = AccountStatus.INVITED;
}
