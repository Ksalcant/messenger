package com.example.auth_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames="email")// unique constraint to avoid race conditions
)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private Instant createdAt = Instant.now();

    // --- Getters --- 
    public UUID getId() { return id; } 
    public String getEmail() { return email; }
    public String getPasswordHash() {return passwordHash;}
     // --- Setters ---

      public void setEmail(String email) { this.email = email; }
      public void setPasswordHash(String pwd){
        this.passwordHash = pwd;


      }
}
