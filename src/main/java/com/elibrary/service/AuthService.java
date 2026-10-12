// 文件路径: D:\Download\eLibaray\src\main\java\com\elibrary\service\AuthService.java
package com.elibrary.service;

import com.elibrary.model.dto.LoginDTO;
import com.elibrary.model.dto.RegisterDTO;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);
}