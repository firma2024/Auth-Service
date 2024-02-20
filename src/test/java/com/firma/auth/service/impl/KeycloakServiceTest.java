package com.firma.auth.service.impl;

import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.security.KeycloakSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {


    @Mock
    private KeycloakSecurityUtil keycloakUtil;


    @InjectMocks
    private KeycloakService keycloakService;

    @Mock
    private Keycloak keycloak;
    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;

    @Captor
    private ArgumentCaptor<String> realmCaptor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test to verify that the method createUserWithRole returns a 201 status code
     * @throws ErrorDataServiceException
     */

    @Test
    public void deleteUser() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8081")
                .realm("firma")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username("admin")
                .password("admin")
                .build();
        when(keycloakUtil.getKeycloakInstance()).thenReturn(keycloak);
        keycloakService.deleteAccount("userId");
    }
   @Test
   public void forgotPassword() {
       Keycloak keycloak = KeycloakBuilder.builder()
               .serverUrl("http://localhost:8081")
               .realm("firma")
               .clientId("admin-cli")
               .grantType(OAuth2Constants.PASSWORD)
               .username("admin")
               .password("admin")
               .build();
       when(keycloakUtil.getKeycloakInstance()).thenReturn(keycloak);

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
        assertTrue(userRepresentation.isEnabled());
        assertFalse(userRepresentation.isEmailVerified());
        CredentialRepresentation credential = userRepresentation.getCredentials().get(0);
        credential.setTemporary(true);
        assertEquals(CredentialRepresentation.PASSWORD, credential.getType());
        assertTrue(credential.isTemporary());
        assertEquals(userRequest.getPassword(), credential.getValue());
    }


}