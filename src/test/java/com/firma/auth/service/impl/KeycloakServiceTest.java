package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.security.KeycloakSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {
    @InjectMocks
    KeycloakService keycloakService;

    @Mock
    private Keycloak keycloak;

    @Mock
    KeycloakSecurityUtil keycloakSecurityUtil;
    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    LogicService logicService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test

    public void testCreateUserWithRole_Success() {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        // Given
        UserRequest user = new UserRequest();
        user.setNombres("John Doe");
        user.setCorreo("john.doe@example.com");
        user.setTelefono(BigInteger.valueOf(1234567890));
        user.setIdentificacion(BigInteger.valueOf(123456789));
        user.setUsername("johndoe");
        user.setPassword("password");
        user.setTipoDocumento("ID");
        user.setEspecialidades(new HashSet<>(Arrays.asList("especialidad1", "especialidad2")));
        user.setFirmaId(1);

        String role = "ADMIN";
    }

    @Test
    public void testGetAccessToken_Success() throws ErrorDataServiceException {
        // Given
        String accessToken = "dummy_access_token";
        String username = "dummy_username";
        String password = "dummy_password";
        AuthenticationRequest request = new AuthenticationRequest(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(createResponseMap(accessToken), HttpStatus.OK);
        when(restTemplate.exchange(any(RequestEntity.class), eq(Map.class))).thenReturn(responseEntity);
        when(logicService.getRole(username)).thenReturn(new Role("1","dummy_role"));

        // When
        TokenResponse tokenResponse = keycloakService.getAccessToken(request);

        // Then
        assertNotNull(tokenResponse);
        assertEquals(accessToken, tokenResponse.getAccess_token());
        assertEquals("dummy_role", tokenResponse.getRole());
    }

    // Método de utilidad para crear un mapa de respuesta simulado
    private Map<String, Object> createResponseMap(String accessToken) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("access_token", accessToken);
        // Agrega más claves y valores según sea necesario
        return responseMap;
    }

    @Test
    public void testMapUserRep() {
        // Given
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("johndoe");
        userRequest.setNombres("John Doe");
        userRequest.setCorreo("john.doe@example.com");
        userRequest.setPassword("password");
        userRequest.setFirmaId(1);
        // When
        UserRepresentation userRepresentation = keycloakService.mapUserRep(userRequest);
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);
        userRepresentation.setRequiredActions(Arrays.asList("VERIFY_EMAIL", "UPDATE_PASSWORD"));
        // Then
        assertEquals(userRequest.getUsername(), userRepresentation.getUsername());
        assertEquals(userRequest.getNombres(), userRepresentation.getFirstName());
        assertEquals(userRequest.getCorreo(), userRepresentation.getEmail());
        assertTrue(userRepresentation.isEnabled());
        assertFalse(userRepresentation.isEmailVerified());
        assertEquals(2, userRepresentation.getRequiredActions().size());
        assertEquals(1, userRepresentation.getCredentials().size());
        CredentialRepresentation credential = userRepresentation.getCredentials().get(0);
        credential.setTemporary(true);
        assertEquals(CredentialRepresentation.PASSWORD, credential.getType());
        assertTrue(credential.isTemporary());
        assertEquals(userRequest.getPassword(), credential.getValue());
    }







}