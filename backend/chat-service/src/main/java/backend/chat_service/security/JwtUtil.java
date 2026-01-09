package backend.chat_service.security;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


import jakarta.annotation.PostConstruct;

/**
 * JwtUtil
 *
 * Validates and parses JWTs issued by auth-service.
 * chat-service uses this to authenticate WebSocket connections during the handshake.
 */

@Component
public class JwtUtil{
    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    void init(){
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public UUID extractUserId(String token){
       // String subject = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
        String subject = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
        
        return UUID.fromString(subject);
    }
}