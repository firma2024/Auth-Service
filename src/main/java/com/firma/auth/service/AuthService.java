package com.firma.auth.service;

import com.firma.auth.dto.AuthenticationRequest;
import com.firma.auth.dto.TokenResponse;
import com.firma.auth.dto.User;
import org.keycloak.representations.idm.UserRepresentation;

public interface AuthService {
    UserRepresentation getUserById(String userId);
    UserRepresentation mapUserRep(User user);
    TokenResponse getAccessToken(AuthenticationRequest request);

    void emailVerification(String userId);
    void forgotPassword(String username);
    boolean deleteAccount(String userId);

}
