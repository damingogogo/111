package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "select id, username, real_name, role, phone, avatar_url from admin_users where username = ? and password = ? and status = '启用'",
                username, password);
        if (users.isEmpty()) {
            return ApiResponse.fail("账号或密码错误");
        }
        String token = Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return ApiResponse.ok(Map.of("token", token, "user", users.get(0)));
    }
}
