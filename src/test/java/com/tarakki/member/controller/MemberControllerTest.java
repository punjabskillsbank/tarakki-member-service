package com.tarakki.member.controller;


import com.tarakki.member.dto.MemberUpdateDTO;
import com.tarakki.member.exception.MemberNotFoundException;
import com.tarakki.common.dto.MemberDTO;
import com.tarakki.member.dto.MemberRequestDTO;
import com.tarakki.member.dto.MemberSignupResponseDTO;
import com.tarakki.member.exception.MemberEmailAlreadyExistsException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        MemberSignupResponseDTO signupResponse = MemberTestDataFactory.createMemberSignupResponseDTO();
        when(memberService.createMember(any()))
                .thenReturn(signupResponse);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(input.getEmail()))
                .andExpect(jsonPath("$.firstName").value(input.getFirstName()));

        verify(memberService).createMember(any(MemberRequestDTO.class));
    }

    @Test
    void shouldReturnFlatMemberFieldsAndTokenWhenCreatingMember() throws Exception {
        MemberSignupResponseDTO signupResponse = MemberTestDataFactory.createMemberSignupResponseDTO();
        when(memberService.createMember(any()))
                .thenReturn(signupResponse);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(signupResponse.getMemberId().toString()))
                .andExpect(jsonPath("$.lastName").value(signupResponse.getLastName()))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.token").value("mock-signup-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                // flat like before: no nested member object and no password material
                .andExpect(jsonPath("$.member").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(memberService).createMember(any(MemberRequestDTO.class));
    }

    @Test
    void shouldReturnConflictWithoutTokenWhenEmailAlreadyExists() throws Exception {
        when(memberService.createMember(any()))
                .thenThrow(new MemberEmailAlreadyExistsException(input.getEmail()));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Member with email " + input.getEmail() + " already exists."))
                .andExpect(jsonPath("$.token").doesNotExist());

        verify(memberService).createMember(any(MemberRequestDTO.class));
    }

    @Test
    void shouldRejectCreateMemberWithBlankEmail() throws Exception {
        input.setEmail("  ");

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithMalformedEmail() throws Exception {
        input.setEmail("not-an-email");

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithMissingEmail() throws Exception {
        input.setEmail(null);

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithBlankPassword() throws Exception {
        input.setPasswordHash("");

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithBlankFirstName() throws Exception {
        input.setFirstName("");

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithBlankLastName() throws Exception {
        input.setLastName(" ");

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithFirstNameOver100Characters() throws Exception {
        input.setFirstName("a".repeat(101));

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithNullAccountStatus() throws Exception {
        input.setAccountStatus(null);

        assertCreateRejectedAsBadRequest(objectMapper.writeValueAsString(input));
    }

    @Test
    void shouldRejectCreateMemberWithUnknownAccountStatus() throws Exception {
        String body = "{\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@b.com\","
                + "\"passwordHash\":\"secret123\",\"accountStatus\":\"SUSPENDED\"}";

        assertCreateRejectedAsBadRequest(body);
    }

    @Test
    void shouldRejectCreateMemberWithMalformedJson() throws Exception {
        assertCreateRejectedAsBadRequest("{\"firstName\":");
    }

    @Test
    void shouldRejectCreateMemberWithEmptyBody() throws Exception {
        assertCreateRejectedAsBadRequest("");
    }

    // A rejected signup must answer 400 without a token and never reach the service.
    private void assertCreateRejectedAsBadRequest(String body) throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist());

        verifyNoInteractions(memberService);
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
