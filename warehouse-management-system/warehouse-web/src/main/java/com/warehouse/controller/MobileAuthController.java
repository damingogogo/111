package com.warehouse.controller;

import com.warehouse.entity.User;
import com.warehouse.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) Map<String, String> body,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String password,
                                   HttpServletRequest request) {
        // 支持 JSON body 和 form 参数两种方式
        String uname = "";
        String pwd = "";
        if (body != null) {
            uname = body.getOrDefault("username", "").trim();
            pwd = body.getOrDefault("password", "");
        }
        if (uname.isEmpty() && username != null) {
            uname = username.trim();
            pwd = password != null ? password : "";
        }

        if (uname.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "用户名不能为空"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(uname, pwd));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            User user = userMapper.findByUsername(uname);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("username", uname);
            result.put("realName", user != null ? user.getRealName() : "");
            result.put("role", user != null ? user.getRole() : "");
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "用户名或密码错误"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未登录"));
        }

        User user = userMapper.findByUsername(authentication.getName());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("username", authentication.getName());
        result.put("realName", user != null ? user.getRealName() : "");
        result.put("role", user != null ? user.getRole() : "");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
