package com.firma.auth.service;

import com.firma.auth.dto.AuthenticationRequest;
import com.firma.auth.dto.User;
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
