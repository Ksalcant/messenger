package com.example.auth_service.Controllers;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/me")
    public Map<String, Object> me(
        @AuthenticationPrincipal UserDetails userDetails
    ){
        return Map.of("email", userDetails.getUsername());
    }
}
