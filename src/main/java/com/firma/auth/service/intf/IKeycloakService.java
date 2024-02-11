package com.firma.auth.service.intf;

import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

public interface IKeycloakService {
    UserRepresentation getUserById(String userId);
    UserRepresentation mapUserRep(UserRequest user);
    ResponseEntity<?> createUserWithRole(UserRequest user, String role) throws ErrorDataServiceException;
    TokenResponse getAccessToken(AuthenticationRequest request) throws ErrorDataServiceException;
    void emailVerification(String userId);
    void forgotPassword(String username);
    boolean deleteAccount(String userId);
}