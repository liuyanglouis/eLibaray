package com.elibrary.model.dto;

import com.elibrary.model.entity.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBookDTO {

    @NotBlank(message = "书名不能为空")
    private String title;

    @NotBlank(message = "作者不能为空")
    private String author;

    private String isbn;

    private String publisher;

    @NotNull(message = "出版年份不能为空")
    @Positive(message = "出版年份必须为正数")
    private Integer publicationYear;

    private String description;

    @NotNull(message = "分类不能为空")
    private Book.Category category;

    @NotNull(message = "总数量不能为空")
    @Positive(message = "总数量必须为正数")
    private Integer totalCopies = 1;

    private Integer pageCount;

    private String language = "Chinese";

    private String format = "PDF";

    private Long fileSize;

    private String filePath;

    private String coverImagePath;

    private BigDecimal price;
}