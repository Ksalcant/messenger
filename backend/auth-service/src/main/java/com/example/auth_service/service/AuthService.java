package com.example.auth_service.service;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepo;
import com.example.auth_service.repository.UserRepo;
import com.example.auth_service.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepo userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId());
    }
}