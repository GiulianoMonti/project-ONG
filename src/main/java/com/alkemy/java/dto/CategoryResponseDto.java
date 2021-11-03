
package com.alkemy.java.dto;

import java.util.Date;

import com.alkemy.java.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDto {

    private String name;

    private String description;

    private String image;

    private Date createDate;

    private Date lastUpdate;

    public static CategoryResponseDto buildResponse(Category category){

        return CategoryResponseDto.builder()
                .name(category.getName())
                .description(category.getDescription())
                .image(category.getImage())
                .createDate(category.getCreateDate())
                .lastUpdate(category.getLastUpdate())
                .build();

    }
}
