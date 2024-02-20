package com.firma.auth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firma.auth.dto.Role;
import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.security.KeycloakSecurityUtil;
import com.firma.auth.service.intf.ILogicService;
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
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Collections.singletonList;

/**
 * Class KeycloakService which implements the IKeycloakService interface
 * This class is used to manage the Keycloak service, including the creation of users, the generation of access tokens,
 * the password recovery and the deletion of accounts.
 * @see IKeycloakService
 * @see KeycloakSecurityUtil
 * @see ILogicService
 * @see UserRequest
 * @see UserResponse
 * @see Role
 * @see TokenResponse
 * @see ErrorDataServiceException
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
    KeycloakSecurityUtil keycloakUtil;
    private final ILogicService logicService;

    @Autowired
    public KeycloakService(KeycloakSecurityUtil keycloakUtil, ILogicService logicService) {
        this.keycloakUtil = keycloakUtil;
        this.logicService = logicService;
    }

    /**
     * Método para crear un usuario con un rol específico en Keycloak.
     * @param user Usuario a crear.
     * @param role Rol del usuario (ADMIN, ABOGADO, JEFE).
     * @throws ErrorDataServiceException Excepción en caso de error en el servicio de datos.
     * @return ResponseEntity con el usuario creado.
     */

    @Override
    public ResponseEntity<?> createUserWithRole(@RequestBody UserRequest user, String role) throws ErrorDataServiceException {
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

            UserResponse userResponse = UserResponse.builder()
                    .nombres(user.getNombres())
                    .correo(user.getCorreo())
                    .telefono(user.getTelefono())
                    .identificacion(user.getIdentificacion())
                    .username(user.getUsername())
                    .tipoDocumento(user.getTipoDocumento())
                    .especialidades(user.getEspecialidades())
                    .firmaId(user.getFirmaId())
                    .build();

            switch (role) {
                case "ADMIN" -> logicService.addAdmin(userResponse);
                case "ABOGADO" -> logicService.addAbogado(userResponse);
                case "JEFE" -> logicService.addJefe(userResponse);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } else {
            String errorMessage = res.readEntity(String.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
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
    Método para obtener el token de acceso a partir de las credenciales de un usuario.
    @param request Credenciales del usuario.
    @throws ErrorDataServiceException Excepción en caso de error en el servicio de datos.
    @return TokenResponse con el token de acceso y el rol del usuario.
     */

    @Override
    public TokenResponse getAccessToken(AuthenticationRequest request) throws ErrorDataServiceException {
        try{
            // Create the request headers
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
        }catch (Exception e){
            throw new ErrorDataServiceException("Error al obtener el token de acceso");
        }
    }
    public String getRole(String username) {
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        String userId = keycloak.realm(realm).users().search(username).get(0).getId();
        List<RoleRepresentation> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listAll();
        for (RoleRepresentation role : roles) {
            if (role.getName().equals("ADMIN")) {
                return "ADMIN";
            } else if (role.getName().equals("ABOGADO")) {
                return "ABOGADO";
            } else if (role.getName().equals("JEFE")) {
                return "JEFE";
            }
        }
        return null;
    }
}
