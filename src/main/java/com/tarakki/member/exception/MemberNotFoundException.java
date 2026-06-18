package com.tarakki.member.exception;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(UUID userId) {
        super("User not found at id:" +userId);
    }

    public MemberNotFoundException(String email) {
        super("Member not found with email: " + email);
    }
}
