package backend.chat_service.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID; 

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;

/**
 * JwtAuthenticationFilter (Chat-service)
 * Authenticates HTTP requests using Authorization: Bearer <JWT>.
 * If taken is valid, stores userId as the authenticated principal in SecurityContext. 
 * 
 * This is used for HTTP endpoints like fetching message history.
 * (WebSocket handshake autyh is handled separately in ChatWebSocketHandler. )
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil; 

    public JwtAuthenticationFilter(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil; 
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain ) throws ServerException, IOExecption {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return; 
        }

        String token = header.substring(7); 

        UUID userId; 
        try{
            userId = jwtUtil.extractUserId(token);
        }catch(Exception e){
            filterChain.doFilter(request, response); 
            return;
        }

        //Principal = userId (simple and service-independent)
        UsernamePasswordAuthenticationToken  authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);

    }
}
