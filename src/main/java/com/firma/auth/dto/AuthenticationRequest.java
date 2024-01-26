package com.firma.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
