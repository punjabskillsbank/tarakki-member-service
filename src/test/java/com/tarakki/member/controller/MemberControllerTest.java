package com.tarakki.member.controller;


import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.service.MemberService;
import com.tarakki.member.util.MemberTestDataFactory;
import  org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.tarakki.common.exceptionHandling.MemberNotFoundException;

@WebMvcTest(MemberController.class)
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
    void shouldCreateMember() throws Exception {

        when(memberService.createMember(any()))
                .thenReturn(output);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(input.getEmail()))
                .andExpect(jsonPath("$.firstName").value(input.getFirstName()));
    }

    @Test
    void shouldGetMemberByEmail() throws Exception {
        String email = "sahib@gmail.com";
        when(memberService.getMemberByEmail(email))
                .thenReturn(output);

        mockMvc.perform(get("/api/members/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(output.getEmail()))
                .andExpect(jsonPath("$.firstName").value(output.getFirstName()));
    }

    @Test
    void shouldReturn404WhenMemberNotFound() throws Exception {
        String email = "notfound@gmail.com";
        when(memberService.getMemberByEmail(email))
                .thenThrow(new MemberNotFoundException(email));

        mockMvc.perform(get("/api/members/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member not found with email: " + email));
    }
}