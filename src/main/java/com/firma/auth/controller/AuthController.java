package com.firma.auth.controller;


import com.firma.auth.dto.AuthenticationRequest;
import com.firma.auth.dto.User;
import com.firma.auth.security.KeycloakSecurityUtil;
import com.firma.auth.service.AuthServiceImpl;
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

@RestController
@RequestMapping("/api/auth/")
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

    @PostMapping("/{username}/forgot-password")
    public void forgotPassword(@PathVariable String username) {
        authServiceImpl.forgotPassword(username);
    }

    //agregar el pre autorize para los 3 endpoints.
    //crear el endopoint de olvidar contraseña.
    @PostMapping(value = "/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "ADMIN");
    }

    @PostMapping(value = "/abogado")
    @PreAuthorize("hasAnyAuthority('ADMIN' ,'JEFE')")
    public ResponseEntity<?> createAbogado(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "ABOGADO");
    }

    @PostMapping(value = "/jefe")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> createJefe(@RequestBody User user) {
        return authServiceImpl.createUserWithRole(user, "JEFE");
    }
    @PutMapping(value = "/user")
    public Response updateUser(@RequestBody User user) {
        UserRepresentation userRep = authServiceImpl.mapUserRep(user);
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(user.getId()).update(userRep);
        return Response.ok(user).build();
    }
    @DeleteMapping(value = "/users/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        try {
           if (authServiceImpl.deleteAccount(id))
               return ResponseEntity.status(HttpStatus.OK).body("User deleted");
           else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
