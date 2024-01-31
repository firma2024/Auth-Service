package com.firma.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firma.auth.dto.AuthenticationRequest;
import com.firma.auth.dto.TokenResponse;
import com.firma.auth.dto.User;
import com.firma.auth.dto.UserResponse;
import com.firma.auth.security.KeycloakSecurityUtil;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Service
public class AuthServiceImpl implements AuthService {


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
            UserRepresentation userRepresentation = keycloak.realm(realm).users().search(user.getUserName()).get(0);
            emailVerification(userRepresentation.getId());
            keycloak.realm(realm).users().get(userRepresentation.getId()).resetPassword(mapUserRep(user).getCredentials().get(0));
            String userId = keycloak.realm(realm).users().search(user.getUserName()).get(0).getId();

            RoleRepresentation Role = keycloak.realm(realm).roles().get(role).toRepresentation();

            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(singletonList(Role));
            //Ahora en este punto se tiene que mandar
            //todo el usuario con el rol a el componente de datos para que se guarde en la base de datos
            SendToDataComponent(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } else {
            String errorMessage = res.readEntity(String.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
    }

    @Override
    public void SendToDataComponent(User user) {
        String url = "http://localhost:8082//api/data/usuario/add/abogado";
        RestTemplate restTemplate = new RestTemplate();
        UserResponse userResponse = UserResponse.builder()
                .nombres(user.getFirstName() + " " + user.getLastName())
                .correo(user.getEmail())
                .telefono(user.getTelefono())
                .identificacion(user.getIdentificacion())
                .username(user.getUserName())
                .tipoDocumento(user.getTipoDocumento())
                .especialidades(user.getEspecialidades())
                .firmaId(user.getFirmaId())
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(url, userResponse, String.class);
        response.getBody();
    }

    @Override
    public void emailVerification(String userId) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        keycloak.realm(realm).users().get(userId).executeActionsEmail(singletonList("VERIFY_EMAIL"));
    }

    @Override
    public void forgotPassword(String username) {
         UsersResource usersResource = keycloakUtil.getKeycloakInstance().realm(realm).users();
        List<UserRepresentation> userRepresentations = keycloakUtil.getKeycloakInstance().realm(realm).users().search(username);
        UserRepresentation userRepresentation = userRepresentations.stream().findFirst().orElse(null);
        if (userRepresentation!= null){
            UserResource userResource = usersResource.get(userRepresentation.getId());
            List<String> actions = new ArrayList<>();
            actions.add("UPDATE_PASSWORD");
            userResource.executeActionsEmail(actions);
            return;
        }
        throw new RuntimeException("User not found");
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

    @Override
    public UserRepresentation mapUserRep(User user) {
        UserRepresentation userRep = new UserRepresentation();
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

    @Override
    public TokenResponse getAccessToken(AuthenticationRequest request) {
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
        return TokenResponse.builder()
                .access_token((String) responseMap.get("access_token"))
                .role(null)
                .build();
    }
}
