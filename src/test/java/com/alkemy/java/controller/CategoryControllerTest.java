package com.alkemy.java.controller;

import com.alkemy.java.config.Config;
import com.alkemy.java.dto.*;
import com.alkemy.java.exception.ResourceNotFoundException;
import com.alkemy.java.model.Category;
import com.alkemy.java.repository.UserRepository;
import com.alkemy.java.service.ICategoryService;
import com.alkemy.java.util.UtilPagination;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(Config.class)
@WithMockUser(authorities = {"ROLE_ADMIN"})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICategoryService categoryService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UtilPagination utilPagination;


    @Autowired
    private ObjectMapper mapper;



    @Test
    @WithAnonymousUser
    void should_no_authorized_when_call_getAllCategories() throws Exception {
        //given
        List<Category> categories = new ArrayList<>();
        //when
        when(categoryService.findAllCategories()).thenReturn(categories);
        mockMvc.perform(get("/categories/").contentType(MediaType.APPLICATION_JSON))
        //then
            .andExpect(status().isUnauthorized());
        }


    @Test
    void should_return_error_when_send_invalidID() throws Exception {
    when(categoryService.getCategoryById(anyLong())).thenThrow(new ResourceNotFoundException("Category doesn't exist."));
        //then
        mockMvc.perform(get("/categories/{id}",anyLong())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Category doesn't exist."))
                .andDo(print());
    }

    @Test
    void sould_return_category_saved_createCategory() throws Exception {
        //given
        CategoryRequestDto categoryRequest = new CategoryRequestDto("new category2","new description2","new image2");
        CategoryResponseDto categoryResp = new CategoryResponseDto("new category2","new description2","new image2",new Date(),new Date());
        //when
        when(categoryService.createCategory(categoryRequest)).thenReturn(categoryResp);
        //then
        mockMvc.perform(post("/categories")
                .content(mapper.writeValueAsString(categoryRequest))
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(categoryResp.getName()));
    }

    @Test
    @WithAnonymousUser
    void should_return_Unauthorized() throws Exception {
        //given
        CategoryRequestDto categoryRequest = new CategoryRequestDto("new category2","new description2","new image2");
        //when
        when(categoryService.createCategory(categoryRequest)).thenReturn(new CategoryResponseDto());
        //then
        mockMvc.perform(post("/categories")
                .content(mapper.writeValueAsString(categoryRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }
    @Test
    void should_return_error_name_is_empty() throws Exception {
        //given
        CategoryRequestDto categoryRequest = new CategoryRequestDto("","new description2","new image2");
        //when
        when(categoryService.createCategory(categoryRequest)).thenReturn(new CategoryResponseDto());
        //then
        mockMvc.perform(post("/categories")
                .content(mapper.writeValueAsString(categoryRequest))
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.messages[0]").value("Field name should not be null or empty"));
    }

    @Test
    void should_return_category_getCategoryById() throws Exception {
        //given
        CategoryResponseDto categoryDto = new CategoryResponseDto("first category","description","url_image",new Date(),new Date());
        //when
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDto);
        //then
        mockMvc.perform(get("/categories/{id}",1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("first category"))
                .andExpect(jsonPath("$.description").value("description"));


    }

    @Test
    void should_return_first_page_getCategoriesPageable() throws Exception {
        CategoryListRespDto categoryDto1 = new CategoryListRespDto("first category");
        CategoryListRespDto categoryDto2 = new CategoryListRespDto("second category");
        PageDto<CategoryListRespDto> pageDto = new PageDto<>();

        List<CategoryListRespDto> memberList = new ArrayList<>();
        memberList.add(categoryDto1);
        Page<CategoryListRespDto> memberTesting = new PageImpl(memberList);

        when(categoryService.getPageableCategory(any(Pageable.class))).thenReturn(memberTesting);

        pageDto.setContent(Arrays.asList(categoryDto1,categoryDto2));
           when(categoryService.getPageableCategory(any(Pageable.class))).thenReturn(memberTesting);

        mockMvc.perform(get("/categories")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("first category"));
    }
    @Test
    @WithAnonymousUser
    void should_return_unauthorized()throws Exception {
        mockMvc.perform(get("/categories")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }
    @Test
    void return_id_notfound_updateCategory() throws Exception {
        //given
         CategoryResponseDto category = new CategoryResponseDto();
         when(categoryService.updateCategory(any(CategoryResponseDto.class),any(Long.class))).thenThrow(new ResourceNotFoundException("id not found"));
         mockMvc.perform(put("/categories/{id}",1L)
                 .content(mapper.writeValueAsString(category))
                 .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNotFound());
    }


    @Test
    void return_resource_notFound_exception_deleteCategory() throws Exception {
        //given
        mockMvc.perform(delete("/categories/{id}",1L))
                .andExpect(status().isForbidden());

    }
}