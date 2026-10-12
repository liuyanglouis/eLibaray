package com.elibrary.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class Book {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private Category category;
    private Integer totalCopies = 1;
    private Integer availableCopies = 1;
    private Integer pageCount;
    private String language = "Chinese";
    private String format = "PDF";
    private Long fileSize;
    private String filePath;
    private String coverImagePath;
    private BigDecimal price;
    private BigDecimal rating;
    private Integer reviewCount = 0;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isAvailable() {
        return isActive && availableCopies > 0;
    }

    public void borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public enum Category {
        FICTION,
        NON_FICTION,
        SCIENCE,
        TECHNOLOGY,
        HISTORY,
        ART,
        LITERATURE,
        EDUCATION,
        CHILDREN,
        BUSINESS,
        HEALTH,
        TRAVEL,
        COOKING,
        SPORTS,
        OTHER
    }
}