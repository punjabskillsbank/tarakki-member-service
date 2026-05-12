package com.tarakki.member.exception;

public class MemberEmailAlreadyExistsException extends RuntimeException {
    public MemberEmailAlreadyExistsException(String email) {
        super("Member with email " + email + " already exists.");
    }
}
