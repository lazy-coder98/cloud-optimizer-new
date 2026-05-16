package com.cloud.optimizer.controller;

import java.time.Instant;
import java.util.Map;
import com.cloud.optimizer.model.AppUser;
import com.cloud.optimizer.model.LoginRequest;
import com.cloud.optimizer.repository.UserRepository;
import com.cloud.optimizer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        String password = request.getPassword();

        if (username.length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }

        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        return Map.of("token", jwtUtil.generateToken(username));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String username = normalizeUsername(request.getUsername());

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return Map.of("token", jwtUtil.generateToken(username));
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
