package com.Firma.Auth.service;

import com.Firma.Auth.dto.AuthenticationRequest;
import com.Firma.Auth.dto.User;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    UserRepresentation getUserById(String userId);
    UserRepresentation mapUserRep(User user);
    String getAccessToken(AuthenticationRequest request);
    ResponseEntity<?> createUserWithRole(User user, String role);
    void emailVerification(String userId);
    void forgotPassword(String username);
    boolean deleteAccount(String userId);

}
