package com.intelliservice.backend.service;

import com.intelliservice.backend.config.JwtUtil;
import com.intelliservice.backend.mapper.UserMapper;
import com.intelliservice.backend.model.dto.LoginRequest;
import com.intelliservice.backend.model.dto.RegisterRequest;
import com.intelliservice.backend.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    /** 验证密码，生成 JWT Token 返回 */
    public String login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return jwtUtil.generateToken(user.getUsername());
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole() != null ? user.getRole().toUpperCase() : "USER")
                .build();
    }
}
