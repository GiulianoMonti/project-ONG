package com.alkemy.java.service;

import com.alkemy.java.dto.SlideResponseDto;

import java.util.List;

public interface ISlideService {

    List<SlideResponseDto> getAllSlide();


    SlideResponseDto getById(Long id);

    void delete(Long id);

}