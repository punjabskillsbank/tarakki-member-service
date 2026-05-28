package com.tarakki.member.dto;

import com.tarakki.common.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Member data transfer object")
public class MemberDTO {

    @Schema(description = "Unique member identifier")
    private UUID memberId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Member first name")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Member last name")
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "Member email address")
    private String email;

    @Schema(description = "S3 key for profile photo")
    private String profilePhotoS3Key;

    @NotNull
    @Schema(description = "Current account status")
    private AccountStatus accountStatus = AccountStatus.INVITED;
}