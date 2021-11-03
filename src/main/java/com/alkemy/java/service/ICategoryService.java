
package com.alkemy.java.service;

import com.alkemy.java.dto.CategoryRequestDto;
import com.alkemy.java.dto.CategoryResponseDto;
import com.alkemy.java.model.Category;
import com.alkemy.java.dto.CategoryProjectionDto;
import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {


    CategoryResponseDto getCategoryById(Long categoryId) throws NotFoundException;
    CategoryResponseDto createCategory (CategoryRequestDto categoryRequest);
    void deleteCategory(Long id);
    CategoryResponseDto updateCategory(CategoryResponseDto dto, Long id);
    List<Category> findAllCategories();
    Page<CategoryProjectionDto> getPageableCategory(Pageable pageable);
}
