package com.example.auth_service.security;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.auth_service.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.auth_service.security.JwtUtil;

/**
 * JwtAuthenticationFilter
 *
 * This filter runs once per HTTP request and is responsible for:
 * 1. Reading the Authorization header (Bearer JWT)
 * 2. Validating the JWT signature and expiration
 * 3. Extracting the authenticated user's identity (userId)
 * 4. Loading user details from the database
 * 5. Populating the Spring SecurityContext with an authenticated principal
 *
 * Without this filter, Spring Security treats all requests as anonymous,
 * which results in 401/403 responses for protected endpoints.
 *
 * This filter MUST be registered in the SecurityFilterChain and MUST run
 * before UsernamePasswordAuthenticationFilter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Read the Authorization header
        String header = request.getHeader("Authorization");

        // If no JWT is present, continue without authentication
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract raw JWT from header
        String token = header.substring(7);

        // Validate JWT signature and expiration
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract authenticated user's ID from JWT
        UUID userId = jwtUtil.extractUserId(token);

        // Load user details from persistence layer
        UserDetails userDetails =
                userDetailsService.loadUserById(userId);

        // Create authenticated security token
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // Store authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue request processing
        filterChain.doFilter(request, response);
    }
}