package com.example.auth_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         System.out.println(">>> CUSTOM SECURITY CONFIG ACTIVE <<<");
        http
            // This disables ALL security
            .csrf(csrf -> csrf.disable())
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                //.requestMatchers("/auth/register","/auth/login").permitAll()
                .requestMatchers("/auth/**").permitAll().anyRequest().authenticated()
            )
            // Disable default Login mechanisms (we use JWT Later)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
    
}