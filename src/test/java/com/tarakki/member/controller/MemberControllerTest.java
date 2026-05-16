package com.tarakki.member.controller;

import com.tarakki.common.exceptionHandling.MemberNotFoundException;
import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.exception.GlobalExceptionHandler;
import com.tarakki.member.service.MemberService;
import com.tarakki.member.util.MemberTestDataFactory;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MemberController.class, GlobalExceptionHandler.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberDTO input;
    private MemberDTO output;

    @BeforeEach
    void setUp() {
        input = MemberTestDataFactory.createMemberDTO();
        output = MemberTestDataFactory.createMemberDTO();
    }

    @Test
    void shouldReturn404WhenMemberNotFound() throws Exception {
        UUID memberId = MemberTestDataFactory.createRandomUUID();
        when(memberService.getMemberById(memberId)).thenThrow(new MemberNotFoundException(memberId));

        mockMvc.perform(get("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateMember() throws Exception {
        when(memberService.createMember(any())).thenReturn(output);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());
    }
}