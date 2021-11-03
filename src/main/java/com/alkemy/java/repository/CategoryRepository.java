package com.alkemy.java.repository;

import com.alkemy.java.dto.CategoryProjectionDto;
import com.alkemy.java.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    @Query(value = "SELECT name FROM categories",
            nativeQuery = true)
    Page<CategoryProjectionDto> getName(Pageable pageable);
}
