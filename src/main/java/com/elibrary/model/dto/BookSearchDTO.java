package com.elibrary.model.dto;

import com.elibrary.model.entity.Book;
import lombok.Data;

@Data
public class BookSearchDTO {
    private String title;
    private String author;
    private Book.Category category;
    private Integer minPublicationYear;
    private Integer maxPublicationYear;
    private Boolean availableOnly = false;
    private Double minRating;
    private String publisher;
    private String language;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private Integer page = 0;
    private Integer size = 20;

    // 获取偏移量用于分页
    public Integer getOffset() {
        return page * size;
    }

    public Integer getLimit() {
        return size;
    }
}