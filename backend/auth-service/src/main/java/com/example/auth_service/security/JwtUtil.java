package com.example.auth_service.security;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(UUID userId){
        Date now = new Date();

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + 86400000))
            .signWith(key)
            .compact();
    }
}
