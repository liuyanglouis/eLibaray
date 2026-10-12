// 文件路径: D:\Download\eLibaray\src\main\java\com\elibrary\service\impl\AuthServiceImpl.java
package com.elibrary.service.impl;

import com.elibrary.common.exception.BusinessException;
import com.elibrary.common.exception.ErrorCode;
import com.elibrary.common.security.JwtUtil;
import com.elibrary.mapper.UserMapper;
import com.elibrary.model.dto.LoginDTO;
import com.elibrary.model.dto.RegisterDTO;
import com.elibrary.model.entity.User;
import com.elibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        System.out.println("loginDTO.getPassword() = " + loginDTO.getPassword());
        System.out.println("user.getPassword() = " + user.getPassword());
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!user.getIsActive()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole().name());
        return result;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        if (userMapper.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "用户名已存在");
        }

        if (userMapper.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setFullName(registerDTO.getFullName());
        user.setPhoneNumber(registerDTO.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setIsActive(true);
        user.setMaxBorrowCount(5);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
        // 输出类似：$2a$10$XbG...（一长串真实哈希）
    }
}