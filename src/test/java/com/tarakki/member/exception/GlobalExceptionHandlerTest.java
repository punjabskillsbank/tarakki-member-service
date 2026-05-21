package com.tarakki.member.exception;

import com.tarakki.common.exceptionHandling.MemberNotFoundException;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new com.tarakki.member.exception.GlobalExceptionHandler())
                .build();
    }
    @Test
    void shouldHandleMemberEmailAlreadyExistsException() throws Exception {
        String testEmail = MemberTestDataFactory.createMemberDTO().getEmail();
        mockMvc.perform(get("/test/already-exists"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Member with email " + testEmail + " already exists."));
    }
    @Test
    void shouldHandleMemberNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound());
    }

    @RestController
    static class TestController {
        @GetMapping("/test/already-exists")
        public void throwAlreadyExists() {
            String testEmail = MemberTestDataFactory.createMemberDTO().getEmail();
            throw new MemberEmailAlreadyExistsException(testEmail);
            
        }
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            UUID randomId = MemberTestDataFactory.createRandomUUID();
            throw new MemberNotFoundException(randomId);
        }
    }
}