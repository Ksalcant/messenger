package com.example.auth_service.dto;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }
}
