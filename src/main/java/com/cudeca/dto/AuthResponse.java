package com.cudeca.dto;

public class AuthResponse {
    private String jwtToken;

    public AuthResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    // Getter
    public String getJwtToken() {
        return jwtToken;
    }
    // Setter
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}