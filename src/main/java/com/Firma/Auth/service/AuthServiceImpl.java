package com.Firma.Auth.service;

import com.Firma.Auth.dto.AuthenticationRequest;
import com.Firma.Auth.dto.User;
import com.Firma.Auth.security.KeycloakSecurityUtil;
import com.Firma.Auth.tool.ObjectToUrlEncodedConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Service
public class AuthServiceImpl implements AuthService{


    @Value("${server-url}")
    private  String authServerUrl;

    @Value("${realm}")
    private  String realm;

    @Value("${keycloak.resource.client-id}")
    private  String clientId;

    @Value("${grant-type}")
    private  String grantType;

    @Value("${keycloak.credentials.secret}")
    private  String clientSecret;
    KeycloakSecurityUtil keycloakUtil;
    @Autowired
    public AuthServiceImpl(KeycloakSecurityUtil keycloakUtil) {
        this.keycloakUtil = keycloakUtil;
    }

    public ResponseEntity<?> createUserWithRole(@RequestBody User user, String role) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        UserRepresentation userRep = mapUserRep(user);
        Response res = keycloak.realm(realm).users().create(userRep);

        if (res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            UserRepresentation userRepresentation = getUserById(userRep.getId());
            emailVerification(userRepresentation.getId());
            keycloak.realm(realm).users().get(userRepresentation.getId()).resetPassword(mapUserRep(user).getCredentials().get(0));
            String userId = keycloak.realm(realm).users().search(user.getUserName()).get(0).getId();
            RoleRepresentation Role = keycloak.realm(realm).roles().get(role).toRepresentation();
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(singletonList(Role));
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(res.getStatus()).build();
        }
    }
    @Override
    public void emailVerification(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(userId).executeActionsEmail(singletonList("VERIFY_EMAIL"));
    }

    @Override
    public void forgotPassword(String username) {
    }

    @Override
    public boolean deleteAccount(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        try {
            if (getUserById(userId) != null) {
                keycloak.realm(realm).users().delete(userId);
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

    public UserRepresentation mapUserRep(User user) {
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

    public String getAccessToken(AuthenticationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", grantType);
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("username", request.getUsername());
        requestBody.add("password", request.getPassword());
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .headers(headers)
                .body(requestBody);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);
        Map responseMap = responseEntity.getBody();
        assert responseMap != null;
        return (String) responseMap.get("access_token");
    }

}