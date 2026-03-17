package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User selectByUsername(String username);
}
