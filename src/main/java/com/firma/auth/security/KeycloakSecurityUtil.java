package com.firma.auth.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * class to get keycloak instance
 *
 */
@Component
public class KeycloakSecurityUtil {

    private Keycloak keycloak;
    @Value("${server-url}")
    private String serverUrl;
    @Value("${realm}")
    private String realm;
    @Value("${client-id}")
    private String clientId;
    @Value("${name}")
    private String username;
    @Value("${password}")
    private String password;

    /**
     * get keycloak instance
     *
     * @return Keycloak
     */
    public Keycloak getKeycloakInstance() {
        if (keycloak == null) {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(username)
                    .password(password)
                    .build();
        }
        return keycloak;
    }
}
