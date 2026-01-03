package com.example.auth_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
   Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
