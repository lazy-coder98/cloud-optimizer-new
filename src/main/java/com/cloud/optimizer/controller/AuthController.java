package com.cloud.optimizer.controller;

import com.cloud.optimizer.model.LoginRequest;
import com.cloud.optimizer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public java.util.Map<String, String> login(@RequestBody LoginRequest request) {

        System.out.println("USERNAME = " + request.getUsername());
        System.out.println("PASSWORD = " + request.getPassword());

        if (!"admin".equals(request.getUsername())
                || !"admin123".equals(request.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(request.getUsername());

        return java.util.Map.of("token", token);
    }
}