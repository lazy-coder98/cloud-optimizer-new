package com.cloud.optimizer.controller;

import com.cloud.optimizer.model.LoginRequest;
import com.cloud.optimizer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.auth.username}")
    private String configuredUsername;

    @Value("${app.auth.password}")
    private String configuredPassword;

    @PostMapping("/login")
    public java.util.Map<String, String> login(@RequestBody LoginRequest request) {

        if (!configuredUsername.equals(request.getUsername())
                || !configuredPassword.equals(request.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(request.getUsername());

        return java.util.Map.of("token", token);
    }
}
