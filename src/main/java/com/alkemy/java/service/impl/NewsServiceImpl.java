
package com.alkemy.java.service.impl;

import com.alkemy.java.exception.ResourceNotFoundException;
import com.alkemy.java.model.News;
import com.alkemy.java.repository.NewsRepository;
import com.alkemy.java.service.INewsService;
import java.util.Locale;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.alkemy.java.dto.NewsRequestDto;
import com.alkemy.java.dto.NewsResponseDto;
import com.alkemy.java.exception.BadRequestException;
import com.alkemy.java.exception.Exception;
import com.alkemy.java.repository.CategoryRepository;


@Service
public class NewsServiceImpl implements INewsService{

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MessageSource messageSource;

    @Value ("error.news.service.dont.exist")
     String messageDontExist;

    @Override
    public void deleteNews(Long id) {
        News news = findById(id);

        if (news == null) {
            throw new ResourceNotFoundException(messageSource.getMessage(messageDontExist, null, Locale.getDefault()));
        }

        newsRepository.deleteById(id);
    }

    @Override
    public NewsResponseDto createNews(NewsRequestDto newsDto) {

        if(newsRepository.findByName(newsDto.getName()) != null)
            throw new Exception(messageSource.getMessage("error.news.already.exist", null, Locale.getDefault()));

        if (!categoryRepository.existsById((long) newsDto.getCategoryId()))
            throw new BadRequestException(messageSource.getMessage("error.category.doesnt.exist", null, Locale.getDefault()));

        News newsToSave = NewsRequestDto.dtoToNews(newsDto);

        newsToSave.setCreateDate(new Date());
        newsToSave.setUpdateDate(new Date());
        newsToSave.setDeleted(false);

        newsToSave.setCategory(categoryRepository.findById((long) newsDto.getCategoryId()).get());

        News news = newsRepository.save(newsToSave);

        return NewsResponseDto.newsToDto(news);
    }

    private News findById(Long id){
        return newsRepository.findById(id).orElse(null);
    }

}