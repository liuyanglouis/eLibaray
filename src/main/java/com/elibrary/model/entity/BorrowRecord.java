package com.elibrary.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class BorrowRecord {

    private Long id;
    private User user;
    private Book book;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Status status = Status.BORROWED;
    private Integer renewCount = 0;
    private Integer maxRenewCount = 2;
    private Integer overdueDays = 0;
    private BigDecimal fineAmount = BigDecimal.ZERO;
    private Boolean isFinePaid = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDate) && status == Status.BORROWED;
    }

    public boolean canRenew() {
        return status == Status.BORROWED &&
               renewCount < maxRenewCount &&
               !isOverdue();
    }

    public void calculateOverdue() {
        if (isOverdue() && status == Status.BORROWED) {
            overdueDays = (int) java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
            if (overdueDays < 0) {
                overdueDays = 0;
            }
        }
    }

    public enum Status {
        BORROWED,
        RETURNED,
        OVERDUE,
        LOST
    }
}