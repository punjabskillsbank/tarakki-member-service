package com.tarakki.member.controller;


import com.tarakki.common.exceptionHandling.MemberNotFoundException;
import com.tarakki.member.dto.MemberDTO;
import com.tarakki.member.service.MemberService;
import com.tarakki.member.util.MemberTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;


import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    UUID memberId = UUID.randomUUID();

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
    void testGetMemberDetailsByMemberId() throws Exception {

        when(memberService.getMemberDetailsByMemberId(memberId))
                .thenReturn(output);

        mockMvc.perform(get("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(output.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(output.getLastName()))
                .andExpect(jsonPath("$.email").value(output.getEmail()));
    }

    @Test
    void shouldReturnNotFoundWhenMemberDetailsNotFound() throws Exception {
        when(memberService.getMemberDetailsByMemberId(memberId))
                .thenThrow(new MemberNotFoundException(memberId));

        mockMvc.perform(get("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "User not found at id:"+ memberId));
    }
}