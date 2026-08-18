package com.tarakki.member.controller;


import com.tarakki.member.dto.MemberUpdateDTO;
import com.tarakki.member.exception.MemberNotFoundException;
import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;
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


import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MemberRequestDTO input;
    private MemberDTO output;

    UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        input = MemberTestDataFactory.createMemberRequestDTO();
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
        String email = output.getEmail();
        when(memberService.getMemberByEmail(email))
                .thenReturn(output);

        mockMvc.perform(get("/api/members/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(output.getEmail()))
                .andExpect(jsonPath("$.firstName").value(output.getFirstName()));
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
    void shouldReturn404WhenMemberNotFound() throws Exception {
        String email = "notfound@gmail.com";
        when(memberService.getMemberByEmail(email))
                .thenThrow(new MemberNotFoundException(email));

        mockMvc.perform(get("/api/members/email/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member not found with email: " + email));
    }

    @Test
    void shouldReturnNotFoundWhenMemberDetailsNotFound() throws Exception {
        when(memberService.getMemberDetailsByMemberId(memberId))
                .thenThrow(new MemberNotFoundException(memberId));

        mockMvc.perform(get("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(
                        "User not found at id:" + memberId));
    }

    @Test
    void getAllMembers_ShouldReturn200() throws Exception {

        MemberDTO member1 = MemberTestDataFactory.createMemberDTO();
        List<MemberDTO> mockList = List.of(member1);

        when(memberService.getAllMembers()).thenReturn(mockList);

        mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value(member1.getFirstName()));
    }

    @Test
    void shouldDeleteMember() throws Exception {
        doNothing().when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/api/members/" + memberId))
                .andExpect(status().isOk())
                .andExpect(content().string("Member deleted successfully with id: " + memberId));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMemberThatDoesNotExist() throws Exception {
        doThrow(new MemberNotFoundException(memberId)).when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/api/members/" + memberId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found at id:" + memberId));
    }

    @Test
    void shouldUpdateMemberByMemberId() throws Exception {
        when(memberService.updateMemberByMemberId(eq(memberId), any(MemberUpdateDTO.class))).thenReturn(output);

        mockMvc.perform(patch("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(output.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(output.getLastName()))
                .andExpect(jsonPath("$.email").value(output.getEmail()));
    }

}
