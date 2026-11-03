package com.tarakki.member.dto;

import com.tarakki.common.enums.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberUpdateDTO {

    @Size(max = 100)
    private String firstName;


    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    private String profilePhotoS3Key;

}
