package com.school.School_Management_System.dto;

public class LoginResponse {
    private String token;

    public LoginResponse() {}
    public LoginResponse(String token) { this.token = token; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
