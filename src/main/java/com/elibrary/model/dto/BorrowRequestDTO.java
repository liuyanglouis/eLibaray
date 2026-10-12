package com.elibrary.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;
}