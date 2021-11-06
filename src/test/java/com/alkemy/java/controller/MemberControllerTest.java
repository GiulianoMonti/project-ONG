package com.alkemy.java.controller;

import com.alkemy.java.config.Config;
import com.alkemy.java.config.MessagesConfig;
import com.alkemy.java.dto.MemberDto;
import com.alkemy.java.dto.MemberRequestDto;
import com.alkemy.java.dto.MemberResponseDto;
import com.alkemy.java.exception.ResourceNotFoundException;
import com.alkemy.java.model.Member;
import com.alkemy.java.repository.MemberRepository;
import com.alkemy.java.service.IMemberService;
import com.alkemy.java.util.UtilPagination;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import({Config.class, MessagesConfig.class})
class MemberControllerTest {

    @MockBean
    private UtilPagination utilPagination;

    @Autowired
    private MessageSource messageSource;

    @MockBean
    private IMemberService service;


    @MockBean
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Pageable pageable;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MemberRequestDto requestDto;
    private MemberResponseDto responseDto;
    private MemberDto member;
    private MemberDto memberEdit;
    private Member memberOriginal;


    @BeforeEach
    void setUp() {

        responseDto = new MemberResponseDto(
                "member",
                "facebook.com",
                "instagram.com",
                "linkedin.com",
                "images",
                "description"
        );
        requestDto = new MemberRequestDto(
                "member",
                "facebook.com",
                "instagram.com",
                "linkedin.com",
                "images",
                "description"
        );

        member = new MemberDto("name", "fb"
                , "ig", "link", "image", "desc");

        memberEdit = new MemberDto();
        memberEdit.setName("updated name");

        memberOriginal = new Member(1, "name", "fb"
                , "ig", "link", "image", "desc", null, null, false);

    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getAllMembers() throws Exception {
        List<MemberResponseDto> memberList = new ArrayList<>();
        memberList.add(responseDto);
        PageImpl memberTesting = new PageImpl(memberList);

        when(service.getAllMembersPageable(any(Pageable.class))).thenReturn(memberTesting);

        mockMvc.perform(get("/members")
                        .accept(MimeTypeUtils.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .content("0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("member")))
                .andExpect(jsonPath("$.content[0].image", is("images")))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void getAllMembersIsForbidden() throws Exception {
        List<MemberResponseDto> memberList = new ArrayList<>();
        memberList.add(responseDto);
        Page<MemberDto> memberTesting = new PageImpl(memberList);

        when(service.getAllMembersPageable(any(Pageable.class))).thenReturn(memberTesting);

        mockMvc.perform(get("/members")
                        .accept(MimeTypeUtils.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden())
                .andDo(print());
    }


    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void createMembers() throws Exception {

        doReturn(responseDto).when(service).createMember(requestDto);

        mockMvc.perform(post("/members")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("member")))
                .andDo(print());

    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void createMembersBadRequest() throws Exception {
        requestDto.setName("12abc");
        doReturn(responseDto).when(service).createMember(requestDto);
        mockMvc.perform(post("/members")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andDo(print());

    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void updateMember() throws Exception {

        when(service.updateMember(member, 1L)).
                thenReturn(memberEdit);

        mockMvc.perform(put("/members/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(member)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("updated name")))
                .andDo(print());
    }

    @WithMockUser(authorities = {"ROLE_USER "})
    @Test
    void updateMemberIsForBidden() throws Exception {
        when(service.updateMember(member, 1L)).
                thenReturn(memberEdit);

        mockMvc.perform(put("/members/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }


    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void updateMemberBadRequest() throws Exception {
        String url = "/members/{id}";

        memberOriginal.setId(777L);

        String JSONRequest = mapToJSON(null);

        given(service.updateMember(null, null)).willThrow(IllegalStateException.class);

        mockMvc.perform(put(url, memberOriginal.getId())
                        .content(JSONRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void deleteMemberById() throws Exception {
        doNothing().when(service).deleteById(2L);

        mockMvc.perform(delete("/members/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Successfully deleted")))
                .andDo(print());
    }

    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @Test
    void deleteMemberByIdNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found!")).when(service).deleteById(1L);

        mockMvc.perform(delete("/members/{id}", 1L))
                .andExpect(status().isNotFound())
                .andDo(print());

    }

    private String mapToJSON(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}