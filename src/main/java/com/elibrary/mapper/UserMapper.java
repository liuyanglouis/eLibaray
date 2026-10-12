package com.elibrary.mapper;

import com.elibrary.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(Long id);

    User selectByUsername(String username);

    User selectByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> selectActiveUsers();

    List<User> selectByRole(@Param("role") String role);

    List<User> searchUsers(@Param("keyword") String keyword);

    List<User> findAllActiveUsers();

    Long countActiveUsers();

    User findActiveUserById(@Param("userId") Long userId);

    int insert(User user);

    int update(User user);

    int delete(Long id);
}