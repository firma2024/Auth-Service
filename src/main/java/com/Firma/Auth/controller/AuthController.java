package com.Firma.Auth.controller;

import com.Firma.Auth.dto.AuthenticationRequest;
import com.Firma.Auth.dto.Role;
import com.Firma.Auth.dto.User;
import com.Firma.Auth.keycloackclient.RoleResource;
import com.Firma.Auth.security.KeycloakSecurityUtil;
import com.Firma.Auth.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Collections.singletonList;

@RestController
@RequestMapping("/firma/auth")
@SecurityRequirement(name = "Keycloak")
public class AuthController {

    KeycloakSecurityUtil keycloakUtil;
    private final AuthService authService;

    @Value("${realm}")
    private String realm;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> getAccessToken(@RequestBody AuthenticationRequest request) {
        String token = authService.getAccessToken(request);
        if (token == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping
    @RequestMapping("/users")
    public List<User> getUsers() {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        List<UserRepresentation> userRepresentations =
                keycloak.realm(realm).users().list();
        return authService.mapUsers(userRepresentations);
    }

    @PostMapping(value = "/admin")
    public Response createAdmin(@RequestBody User user) {
        return authService.createUserWithRole(user, "ADMIN");
    }

    @PostMapping(value = "/abogado")
    public Response createAbogado(@RequestBody User user) {
        return authService.createUserWithRole(user, "ABOGADO");
    }

    @PostMapping(value = "/jefe")
    public Response createJefe(@RequestBody User user) {
        return authService.createUserWithRole(user, "jefe");
    }

    @GetMapping(value = "/users/{id}")
    public User getUser(@PathVariable("id") String id) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        return authService.mapUser(keycloak.realm(realm).users().get(id).toRepresentation());
    }
    @PutMapping(value = "/user")
    public Response updateUser(@RequestBody User user) {
        UserRepresentation userRep = authService.mapUserRep(user);
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(user.getId()).update(userRep);
        return Response.ok(user).build();
    }
    @DeleteMapping(value = "/users/{id}")
    public Response deleteUser(@PathVariable("id") String id) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        try {
            keycloak.realm(realm).users().get(id).remove();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }
    @GetMapping(value = "/users/{id}/roles")
    public List<Role> getRoles(@PathVariable("id") String id) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        return RoleResource.mapRoles(keycloak.realm(realm).users()
                .get(id).roles().realmLevel().listAll());
    }
    @PostMapping(value = "/users/{id}/roles/{roleName}")
    public Response createRole(@PathVariable("id") String id,
                               @PathVariable("roleName") String roleName) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(id).roles().realmLevel().add(singletonList(role));
        return Response.ok().build();
    }



}
