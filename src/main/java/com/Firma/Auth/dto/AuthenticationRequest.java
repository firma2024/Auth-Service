package com.Firma.Auth.dto;

public class AuthenticationRequest {

    private String username;
    private String password;

    public AuthenticationRequest() {
        // Constructor vacío
    }

    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Métodos getters y setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
