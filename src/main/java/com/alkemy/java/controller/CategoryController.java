package com.alkemy.java.controller;

import com.alkemy.java.dto.*;
import com.alkemy.java.exception.InvalidDataException;
import com.alkemy.java.exception.ResourceNotFoundException;
import com.alkemy.java.service.ICategoryService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.alkemy.java.util.UtilPagination;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Locale;


@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryController {

    @Autowired
    UtilPagination utilPagination;

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private MessageSource messageSource;

    @Value("success.deleted")
    private String messageDeleted;


    @Value("error.pagination")
    private String paginationError;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody(required = true) CategoryRequestDto categoryRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            throw new InvalidDataException(bindingResult);

        return new ResponseEntity<>(iCategoryService.createCategory(categoryRequest), HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<CategoryListRespDto>> getAllCategories() {
        List<CategoryListRespDto> categories = iCategoryService.findAllCategories()
                .stream()
                .map(CategoryListRespDto::new)
                .collect(Collectors.toList());
        return categories.isEmpty() ? ResponseEntity.status(HttpStatus.NO_CONTENT).body(categories) : ResponseEntity.ok(categories);
    }


    @GetMapping
    ResponseEntity<?> getCategoriesPageable(@PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 10) Pageable pageable,
                                            @RequestParam(value = "page", defaultValue = "0") int page,HttpServletRequest request) {
        try {

            Page<CategoryListRespDto> result = iCategoryService.getPageableCategory(pageable);
            Map<String, String> links = utilPagination.linksPagination(request, result);
            if (page >= result.getTotalPages() | page < 0)
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(messageSource.getMessage(paginationError, null, Locale.getDefault()));

            PageDto<CategoryListRespDto> response = new PageDto<>();
            response.setContent(result.getContent());
            response.setLinks(links);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoryResponseDto categoryResponseDto, @PathVariable(name = "id") long id) {
        try {
            return new ResponseEntity<>(iCategoryService.updateCategory(categoryResponseDto, id), HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long categoryId) throws NotFoundException {

        return ResponseEntity.ok(iCategoryService.getCategoryById(categoryId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        iCategoryService.deleteCategory(id);
        return new ResponseEntity<>(messageSource.getMessage(messageDeleted, null, Locale.getDefault()), HttpStatus.OK);
    }
}