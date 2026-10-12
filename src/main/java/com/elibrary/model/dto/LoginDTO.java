// 文件路径: D:\Download\eLibaray\src\main\java\com\elibrary\model\dto\LoginDTO.java
package com.elibrary.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}