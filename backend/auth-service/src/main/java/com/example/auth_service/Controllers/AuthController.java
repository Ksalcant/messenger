package com.example.auth_service.Controllers;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.repository.UserRepo;
import com.example.auth_service.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(
            passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    String token = authService.login(request);
    return ResponseEntity.ok(Map.of("token", token));
}
}
