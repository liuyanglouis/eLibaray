package com.elibrary.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportLostRequestDTO {

    @NotNull(message = "借阅记录ID不能为空")
    private Long recordId;

    private String remarks;
}