package com.firma.auth.service;

import com.firma.auth.dto.AuthenticationRequest;
import com.firma.auth.dto.TokenResponse;
import com.firma.auth.dto.User;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    UserRepresentation getUserById(String userId);
    UserRepresentation mapUserRep(User user);
    ResponseEntity<?> createUserWithRole(User user, String role);
    TokenResponse getAccessToken(AuthenticationRequest request);
    void SendToDataComponent(User user);
    void emailVerification(String userId);
    void forgotPassword(String username);
    boolean deleteAccount(String userId);

}
