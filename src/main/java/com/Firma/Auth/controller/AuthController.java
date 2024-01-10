package com.Firma.Auth.controller;

import com.Firma.Auth.dto.AuthenticationRequest;
import com.Firma.Auth.dto.User;
import com.Firma.Auth.security.KeycloakSecurityUtil;
import com.Firma.Auth.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/firma/auth")
@SecurityRequirement(name = "Keycloak")
public class AuthController {

    KeycloakSecurityUtil keycloakUtil;
    private final AuthServiceImpl authServiceImpl;

    @Value("${realm}")
    private String realm;

    @Autowired
    public AuthController(AuthServiceImpl authServiceImpl) {
        this.authServiceImpl = authServiceImpl;
    }
    @PostMapping("/login")
    public ResponseEntity<?> getAccessToken(@RequestBody AuthenticationRequest request) {
        String token = authServiceImpl.getAccessToken(request);
        if (token == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    //agregar el pre autorize para los 3 endpoints.
    //crear el endopoint de olvidar contraseña.
    @PostMapping(value = "/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "ADMIN");
    }

    @PostMapping(value = "/abogado")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<?> createAbogado(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "ABOGADO");
    }

    @PostMapping(value = "/jefe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createJefe(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "jefe");
    }
    @PutMapping(value = "/user")
    public Response updateUser(@RequestBody User user) {
        UserRepresentation userRep = authServiceImpl.mapUserRep(user);
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(user.getId()).update(userRep);
        return Response.ok(user).build();
    }
    @DeleteMapping(value = "/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        try {
           if (authServiceImpl.deleteAccount(id))
               return ResponseEntity.ok().build();
           else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
