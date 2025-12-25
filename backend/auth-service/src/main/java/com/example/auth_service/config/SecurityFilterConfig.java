/**package com.example.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityFilterConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
        // disable CSRF for APIS
        .csrf(csrf -> csrf.disable())
        // allow unauthenticated access to auth endpoints 
        .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/register", "/auth/login").permitAll().anyRequest().authenticated())
        // Disable default login form & basic auth UI
        .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
*/