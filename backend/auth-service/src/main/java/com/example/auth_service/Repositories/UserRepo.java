package com.example.auth_service.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.Entities.User;

public interface UserRepo extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    User save(User user);
}
