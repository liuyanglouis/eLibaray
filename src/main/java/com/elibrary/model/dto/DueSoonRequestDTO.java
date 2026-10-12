package com.elibrary.model.dto;

import lombok.Data;

@Data
public class DueSoonRequestDTO {

    private Long userId;

    private int daysBeforeDue = 3;
}