package com.elibrary.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RenewRequestDTO {

    @NotNull(message = "借阅记录ID不能为空")
    private Long recordId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}