package com.warehouse.mapper;

import com.warehouse.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password, real_name, role, status) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET password = #{password}, real_name = #{realName}, " +
            "role = #{role}, status = #{status} WHERE id = #{id}")
    int update(User user);
}
