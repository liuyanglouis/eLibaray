package com.elibrary.model.dto;

import com.elibrary.model.entity.Book;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private Book.Category category;
    private Integer totalCopies;
    private Integer availableCopies;
    private Integer pageCount;
    private String language;
    private String format;
    private Long fileSize;
    private String filePath;
    private String coverImagePath;
    private BigDecimal price;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 计算属性
    public boolean isAvailable() {
        return isActive && availableCopies > 0;
    }
}