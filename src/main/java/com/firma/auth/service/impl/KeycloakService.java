package com.firma.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firma.auth.model.Rol;
import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorKeycloakServiceException;
import com.firma.auth.security.KeycloakSecurityUtil;
import com.firma.auth.service.intf.IKeycloakService;
import com.firma.auth.tool.ObjectToUrlEncodedConverter;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Class KeycloakService which implements the IKeycloakService interface
 * This class is used to manage the Keycloak service, including the creation of users, the generation of access tokens,
 * the password recovery and the deletion of accounts.
 * @see IKeycloakService
 * @see KeycloakSecurityUtil
 * @see UserRequest
 * @see UserResponse
 * @see Rol
 * @see TokenResponse
 * @see ErrorKeycloakServiceException
 * @see AuthenticationRequest
 */

@Service
public class KeycloakService implements IKeycloakService {

    @Value("${authServerUrl}")
    private  String authServerUrl;

    @Value("${realm}")
    private  String realm;

    @Value("${keycloak.resource.client-id}")
    private  String clientId;

    @Value("${grant-type}")
    private  String grantType;

    @Value("${keycloak.credentials.secret}")
    private  String clientSecret;
    private final KeycloakSecurityUtil keycloakUtil;

    @Value("${api.rol.admin}")
    private String adminRole;
    @Value("${api.rol.jefe}")
    private String jefeRole;
    @Value("${api.rol.abogado}")
    private String abogadoRole;

    @Autowired
    public KeycloakService(KeycloakSecurityUtil keycloakUtil) {
        this.keycloakUtil = keycloakUtil;
    }

    /**
     * Método para crear un usuario con un rol específico en Keycloak.
     * @param user Usuario a crear.
     * @param role Rol del usuario (ADMIN, ABOGADO, JEFE).
     * @return ResponseEntity con el usuario creado.
     */

    @Override
    public ResponseEntity<?> createUserWithRole(@RequestBody UserRequest user, String role) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        UserRepresentation userRep = mapUserRep(user);
        Response res = keycloak.realm(realm).users().create(userRep);

        if (res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            UserRepresentation userRepresentation = keycloak.realm(realm).users().search(user.getUsername()).get(0);
            emailVerification(userRepresentation.getId());
            keycloak.realm(realm).users().get(userRepresentation.getId()).resetPassword(mapUserRep(user).getCredentials().get(0));
            String userId = keycloak.realm(realm).users().search(user.getUsername()).get(0).getId();
            RoleRepresentation Role = keycloak.realm(realm).roles().get(role).toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(singletonList(Role));
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } else {
            String errorMessage = res.readEntity(String.class);
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @Override
    public void emailVerification(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(userId).executeActionsEmail(singletonList("VERIFY_EMAIL"));
    }

    @Override
    public ResponseEntity<?> forgotPassword(String username){
        UsersResource usersResource = keycloakUtil.getKeycloakInstance().realm(realm).users();

        List<UserRepresentation> userRepresentations = usersResource.search(username);
        Optional<UserRepresentation> userOptional = userRepresentations.stream().findFirst();

        if (userOptional.isPresent()) {
            UserRepresentation userRepresentation = userOptional.get();
            UserResource userResource = usersResource.get(userRepresentation.getId());
            List<String> actions = new ArrayList<>();
            actions.add("UPDATE_PASSWORD");
            userResource.executeActionsEmail(actions);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Método para desabilitar una cuenta de usuario en Keycloak.
     * @param userId id del usuario a desabilitar.
     * @return true si la cuenta fue desabilitada, false en caso contrario.
     */

    @Override
    public boolean deleteAccount(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            user.setEnabled(false);
            keycloak.realm(realm).users().get(userId).update(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserRepresentation mapUserRep(UserRequest user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getUsername());
        userRep.setFirstName(user.getNombres());
        userRep.setEmail(user.getCorreo());
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

    /**
    Method to obtain the access token from Keycloak.
    @param request Credentials from the user.
    @throws ErrorKeycloakServiceException if the request fails, an exception is thrown.
    @return TokenResponse with the access token and the role of the user.
     */

    @Override
    public TokenResponse getAccessToken(AuthenticationRequest request) throws ErrorKeycloakServiceException {
        // Create the request headers
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ObjectMapper objectMapper = new ObjectMapper();
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));
            // Create the request body
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", grantType);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("username", request.getUsername());
            requestBody.add("password", request.getPassword());

            // Create the request entity
            RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                    .post(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                    .headers(headers)
                    .body(requestBody);
            ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(requestEntity, responseType);
            Map<String, Object> responseMap = responseEntity.getBody();
            assert responseMap != null;
            String role = getRole(request.getUsername());
            return TokenResponse.builder()
                    .access_token((String) responseMap.get("access_token"))
                    .role(role)
                    .build();
        }catch (HttpClientErrorException e){
            throw new ErrorKeycloakServiceException(e.getMessage(), e.getStatusCode().value());
        }
    }
    public String getRole(String username) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        String userId = keycloak.realm(realm).users().search(username).get(0).getId();
        List<RoleRepresentation> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listAll();
        for (RoleRepresentation role : roles) {
            if (role.getName().equals(adminRole)) {
                return adminRole;
            } else if (role.getName().equals(abogadoRole)) {
                return abogadoRole;
            } else if (role.getName().equals(jefeRole)) {
                return jefeRole;
            }
        }
        return null;
    }
}