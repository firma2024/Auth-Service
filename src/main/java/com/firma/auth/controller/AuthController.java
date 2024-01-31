package com.firma.auth.controller;


import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.service.impl.KeycloakService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
@SecurityRequirement(name = "Keycloak")
public class AuthController {
    private final KeycloakService keycloakService;

    @Autowired
    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> getAccessToken(@RequestBody AuthenticationRequest request) {
        TokenResponse token = keycloakService.getAccessToken(request);
        if (token.getAccess_token() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/{username}/forgot-password")
    public void forgotPassword(@PathVariable String username) {
        keycloakService.forgotPassword(username);
    }

    @PostMapping(value = "/admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserRequest userRequest) {
        return keycloakService.createUserWithRole(userRequest, "ADMIN");
    }

    @PostMapping(value = "/abogado")
    @PreAuthorize("hasAnyAuthority('ADMIN' ,'JEFE')")
    public ResponseEntity<?> createAbogado(@RequestBody UserRequest userRequest) {
        return keycloakService.createUserWithRole(userRequest, "ABOGADO");
    }

    @PostMapping(value = "/jefe")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createJefe(@RequestBody UserRequest userRequest) {
        return keycloakService.createUserWithRole(userRequest, "JEFE");
    }

    @DeleteMapping(value = "/users/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        try {
           if (keycloakService.deleteAccount(id))
               return ResponseEntity.status(HttpStatus.OK).body("User deleted");
           else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
