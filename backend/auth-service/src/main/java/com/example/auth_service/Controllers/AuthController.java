package com.example.auth_service.Controllers;
import com.example.auth_service.Repositories.UserRepo;
import com.example.auth_service.DTOs.RegisterRequest;
import com.example.auth_service.Entities.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AuthController(UserRepo userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
