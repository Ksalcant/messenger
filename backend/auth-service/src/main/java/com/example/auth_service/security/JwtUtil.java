package com.example.auth_service.security;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(UUID userId){
        Date now = new Date();

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + 86400000))
            .signWith(key)
            .compact();
    }

    // ✅ NEW — extract userId
    public UUID extractUserId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return UUID.fromString(subject);
    }

    // ✅ NEW — validate token
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}