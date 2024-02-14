package com.firma.auth.service.impl;

import com.firma.auth.security.KeycloakSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServiceTest {
    @InjectMocks
    KeycloakService keycloakService;
    @Mock
    KeycloakSecurityUtil keycloakSecurityUtil;

    @Mock
    IntegrationService integrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserById() {

    }

    @Test
    void mapUserRep(){
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("test");
        userRepresentation.setEmail("sebasorjuela@gmail.com");
        userRepresentation.setFirstName("Sebastian");
        userRepresentation.setLastName("Orjuela");
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setRequiredActions(null);
        userRepresentation.setAttributes(null);
        userRepresentation.setCredentials(null);
        userRepresentation.setFederatedIdentities(null);
        userRepresentation.setAttributes(null);
        userRepresentation.setAccess(null);
        userRepresentation.setClientRoles(null);
        userRepresentation.setRealmRoles(null);
        userRepresentation.setAttributes(null);
        assertNull(userRepresentation.getAttributes());
    }



}