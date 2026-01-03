package com.example.auth_service.service;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.auth_service.entity.UserEntity;
import com.example.auth_service.repository.UserRepository;

/**
 * CustomUserDetailsService
 *
 * This service is responsible for loading application users for Spring Security.
 *
 * Unlike the default UserDetailsService (which loads users by username/email),
 * this implementation loads users by their UUID, because our JWT stores the
 * userId as the token subject.
 *
 * This class is used by JwtAuthenticationFilter to:
 * 1. Fetch the authenticated user from the database
 * 2. Convert the user into Spring Security's UserDetails format
 *
 * Without this class, JWT authentication cannot complete.
 */

@Service
public class CustomUserDetailsService{
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // Load a use rby thie UUID (from JWT subject)
    public UserDetails loadUserById(UUID userId){
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(()-> new UsernameNotFoundException("User not found" + userId)
        );

        return new User(user.getEmail(),
         user.getPasswordHash(), 
         List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}