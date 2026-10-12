package com.elibrary.model.dto;

import com.elibrary.model.entity.BorrowRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BorrowRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;

    private BorrowRecord.Status status;
    private Integer renewCount;
    private Integer maxRenewCount;
    private Integer overdueDays;
    private BigDecimal fineAmount;
    private Boolean isFinePaid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 计算属性
    public boolean isOverdue() {
        return status == BorrowRecord.Status.BORROWED &&
               LocalDateTime.now().isAfter(dueDate);
    }

    public boolean canRenew() {
        return status == BorrowRecord.Status.BORROWED &&
               renewCount < maxRenewCount &&
               !isOverdue();
    }
}