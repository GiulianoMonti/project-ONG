
package com.alkemy.java.service.impl;

import com.alkemy.java.dto.CategoryRequestDto;
import com.alkemy.java.dto.CategoryResponseDto;
import com.alkemy.java.exception.BadRequestException;
import com.alkemy.java.exception.ResourceNotFoundException;
import com.alkemy.java.model.Category;
import com.alkemy.java.dto.CategoryProjectionDto;
import com.alkemy.java.repository.CategoryRepository;
import com.alkemy.java.service.ICategoryService;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javassist.NotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MessageSource messageSource;

    @Value ("error.service.category.badrequest")
    private String errorBadRequest;

    @Value("error.category.notfound")
    private String notFoundMessage;

    @Value ("error.category.id.not.found")
    private String idNotFoundMessage;

    @Value ("error.service.category.does.not.exist")
    private String errorDoesNotExist;

    @Autowired
    private ModelMapper mapper;


    @Override
    public CategoryResponseDto getCategoryById(Long categoryId) throws NotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(notFoundMessage, null, Locale.getDefault())));

        return CategoryResponseDto.buildResponse(category);
    }

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequest) {

        Category category = categoryRepository.findByName(categoryRequest.getName());

        if (category != null)
            throw new BadRequestException(messageSource.getMessage(errorBadRequest, null, Locale.getDefault()));

        category = mapToEntity(categoryRequest);
        category.setCreateDate(new Date());
        category.setLastUpdate(new Date());

        categoryRepository.save(category);

        return mapToDto(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.delete(categoryRepository.findById(id).orElseThrow( () ->
                new ResourceNotFoundException(messageSource.getMessage(errorDoesNotExist, null, Locale.getDefault()))));

    }

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<CategoryProjectionDto> getPageableCategory(Pageable pageable) {
        return categoryRepository.getName(pageable);
    }

    @Override
    public CategoryResponseDto updateCategory(CategoryResponseDto categoryResponseDto, Long id) {

        Category updatedCategory = categoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(messageSource.getMessage
                        (idNotFoundMessage, null, Locale.getDefault())));

        updatedCategory.setName(categoryResponseDto.getName());
        updatedCategory.setDescription(categoryResponseDto.getDescription());
        updatedCategory.setImage(categoryResponseDto.getImage());
        updatedCategory.setLastUpdate(new Date());
        categoryRepository.save(updatedCategory);

        return CategoryResponseDto.buildResponse(updatedCategory);
    }

    private CategoryResponseDto mapToDto(Category category) {
        return mapper.map(category, CategoryResponseDto.class);
    }

    private Category mapToEntity(CategoryRequestDto categoryRequest) {

        return mapper.map(categoryRequest,Category.class );
    }
}
