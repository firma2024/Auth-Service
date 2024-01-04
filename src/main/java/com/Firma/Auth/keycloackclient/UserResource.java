package com.Firma.Auth.keycloackclient;

import com.Firma.Auth.dto.Role;
import com.Firma.Auth.dto.User;
import com.Firma.Auth.security.KeycloakSecurityUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@RestController
@RequestMapping("/keycloak")
@SecurityRequirement(name = "Keycloak")
public class UserResource {

    KeycloakSecurityUtil keycloakUtil;


    @Autowired
    public UserResource(KeycloakSecurityUtil keycloakUtil) {
        this.keycloakUtil = keycloakUtil;
    }

    @Value("${realm}")
    private String realm;

    @GetMapping
    @RequestMapping("/users")
    public List<User> getUsers() {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        List<UserRepresentation> userRepresentations =
                keycloak.realm(realm).users().list();
        return mapUsers(userRepresentations);
    }
    @PostMapping(value = "/admin")
    public Response createAdmin(@RequestBody User user) {
        return createUserWithRole(user, "ADMIN");
    }

    @PostMapping(value = "/abogado")
    public Response createAbogado(@RequestBody User user) {
        return createUserWithRole(user, "ABOGADO");
    }

    @PostMapping(value = "/jefe")
    public Response createJefe(@RequestBody User user) {
        return createUserWithRole(user, "jefe");
    }

    @GetMapping(value = "/users/{id}")
    public User getUser(@PathVariable("id") String id) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        return mapUser(keycloak.realm(realm).users().get(id).toRepresentation());
    }

    public Response createUserWithRole(@RequestBody User user, String role) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        UserRepresentation userRep = mapUserRep(user);
        Response res = keycloak.realm(realm).users().create(userRep);

        if (res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            UserRepresentation userRepresentation = keycloak.realm(realm).users().search(user.getUserName()).get(0);
            emailVerification(userRepresentation.getId());
            keycloak.realm(realm).users().get(userRepresentation.getId()).resetPassword(mapUserRep(user).getCredentials().get(0));
            keycloak.realm(realm).users().get(userRepresentation.getId()).executeActionsEmail(singletonList("UPDATE_PASSWORD"));
            String userId = keycloak.realm(realm).users().search(user.getUserName()).get(0).getId();
            RoleRepresentation Role = keycloak.realm(realm).roles().get(role).toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(singletonList(Role));
            return Response.ok(user).build();
        } else {
            return Response.status(res.getStatusInfo()).build();
        }
    }

    @PutMapping(value = "/user")
    public Response updateUser(@RequestBody User user) {
        UserRepresentation userRep = mapUserRep(user);
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

    private List<User> mapUsers(List<UserRepresentation> userRepresentations) {
        List<User> users = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userRepresentations)) {
            userRepresentations.forEach(userRep -> users.add(mapUser(userRep)));
        }
        return users;
    }

    private User mapUser(UserRepresentation userRep) {
        User user = new User();
        user.setId(userRep.getId());
        user.setFirstName(userRep.getFirstName());
        user.setLastName(userRep.getLastName());
        user.setEmail(userRep.getEmail());
        user.setUserName(userRep.getUsername());
        return user;
    }

    private UserRepresentation mapUserRep(User user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId(user.getId());
        userRep.setUsername(user.getUserName());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEmail(user.getEmail());
        userRep.setEnabled(true);
        userRep.setEmailVerified(false);
        userRep.setRequiredActions(singletonList("VERIFY_EMAIL"));
        userRep.setRequiredActions(singletonList("UPDATE_PASSWORD"));
        List<CredentialRepresentation> creds = new ArrayList<>();
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(user.getPassword());
        cred.setTemporary(true);
        creds.add(cred);
        userRep.setCredentials(creds);
        return userRep;
    }

    private void emailVerification(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(userId).executeActionsEmail(singletonList("VERIFY_EMAIL"));
    }
}
