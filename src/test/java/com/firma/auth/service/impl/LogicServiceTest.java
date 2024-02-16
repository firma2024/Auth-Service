package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.security.KeycloakSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class LogicServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LogicService logicService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testAddAdminSuccessfully() throws ErrorDataServiceException {
        // Given
        UserResponse user = new UserResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
        ResponseEntity<String> responseEntity = ResponseEntity.ok("Admin added successfully");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), eq(requestEntity), eq(String.class)))
                .thenReturn(responseEntity);
        // When
        String result = logicService.addAdmin(user);
        assertEquals("Admin added successfully", result);
    }
    @Test
    public void testAddAdminThrowsException() {
        // Given
        UserResponse user = new UserResponse();
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String .class)))
                .thenThrow(new RuntimeException("Error occurred"));
        // When/Then
        assertThrows(ErrorDataServiceException.class, () -> logicService.addAdmin(user));
    }

    @Test
    public void testAddAbogado_Success() {
        // Misma lógica que testAddAdmin_Success pero para addAbogado
    }

    @Test
    public void testAddAbogado_Error() {
        // Misma lógica que testAddAdmin_Error pero para addAbogado
    }

    @Test
    public void testAddJefe_Success() throws ErrorDataServiceException {
        // Misma lógica que testAddAdmin_Success pero para addJefe
    }

    @Test
    public void testAddJefe_Error() {
        // Misma lógica que testAddAdmin_Error pero para addJefe
    }



}