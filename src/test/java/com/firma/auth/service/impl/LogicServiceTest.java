package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void testAddAdminSuccess() throws ErrorDataServiceException {

        UserResponse user = new UserResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
        ResponseEntity<String> responseEntity = ResponseEntity.ok("Admin added successfully");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), eq(requestEntity), eq(String.class)))
                .thenReturn(responseEntity);
        String result = logicService.addAdmin(user);
        assertEquals("Admin added successfully", result);
    }

    @Test
    public void testAddAdminThrowsException() {

        UserResponse user = new UserResponse();
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String .class)))
                .thenThrow(new RuntimeException("Error occurred"));

        assertThrows(ErrorDataServiceException.class, () -> logicService.addAdmin(user));
    }


    @Test
    public void testAddJefeSuccess() throws ErrorDataServiceException {

        UserResponse user = new UserResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
        ResponseEntity<String> responseEntity = ResponseEntity.ok("Jefe added successfully");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), eq(requestEntity), eq(String.class)))
                .thenReturn(responseEntity);
        String result = logicService.addJefe(user);
        assertEquals("Jefe added successfully", result);
    }

    @Test
    public void testAddJefeThrowsException() {

        UserResponse user = new UserResponse();
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String .class)))
                .thenThrow(new RuntimeException("Error occurred"));
        assertThrows(ErrorDataServiceException.class, () -> logicService.addJefe(user));
    }

    @Test
    public void testAddAbogadoSuccess() throws ErrorDataServiceException {

        UserResponse user = new UserResponse();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
        ResponseEntity<String> responseEntity = ResponseEntity.ok("Abogado added successfully");
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), eq(requestEntity), eq(String.class)))
                .thenReturn(responseEntity);
        String result = logicService.addAbogado(user);
        assertEquals("Abogado added successfully", result);

    }
    @Test
    public void testAddAbogadoThrowsException() {
        UserResponse user = new UserResponse();
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String .class)))
                .thenThrow(new RuntimeException("Error occurred"));
        assertThrows(ErrorDataServiceException.class, () -> logicService.addAbogado(user));
    }

    @Test
    public void testGetRoleSuccess() throws ErrorDataServiceException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        Role role = new Role();
        role.setNombre("ADMIN");
        role.setId("1");

        ResponseEntity<Role> responseEntity = ResponseEntity.ok(role);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), eq(requestEntity), eq(Role.class))
        ).thenReturn(responseEntity);

        Role result = logicService.getRole("username");

        assertEquals(role, result);
    }
    @Test
    public void testGetRoleThrowsException() {

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Role.class))
        ).thenThrow(new RuntimeException("Error occurred"));

        assertThrows(ErrorDataServiceException.class, () -> logicService.getRole("username"));
    }


}