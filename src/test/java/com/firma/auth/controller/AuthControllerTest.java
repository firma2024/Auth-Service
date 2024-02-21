package com.firma.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@WebAppConfiguration
class AuthControllerTest {

    private final static String AuthURL = "/api/auth";

    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAccessTokenSuccess() throws Exception {
        AuthenticationRequest request = createAuthenticationRequestSuccess();
        MvcResult result = mockMvc.perform(post(AuthURL + "/login").
                accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(request)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
    @Test
    void getAccessTokenFail() throws Exception {
        AuthenticationRequest request = createAuthenticationRequestFail();
        MvcResult result = mockMvc.perform(post(AuthURL + "/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapToJson(request))).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void testForgotPasswordSuccess() throws Exception {
        String username = "danibar";
        MvcResult result = mockMvc.perform(post(AuthURL + "/{username}/forgot-password", username)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
    @Test
    void testForgotPasswordFail() throws Exception {
        String username = "user";
        MvcResult result = mockMvc.perform(post(AuthURL + "/{username}/forgot-password", username)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }
    @Test
    void testCreateAdminSuccess() throws Exception {
        UserRequest userRequest = createUserRequestSuccess();
            MvcResult result = mockMvc.perform(post(AuthURL + "/admin")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(mapToJson(userRequest))).andReturn();
            assertEquals(201, result.getResponse().getStatus());
    }
    @Test
    void testCreateAdminFail() throws Exception {
        UserRequest userRequest = createUserRequestFail();
        MvcResult result = mockMvc.perform(post(AuthURL + "/admin")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    private String mapToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
    private AuthenticationRequest createAuthenticationRequestFail() {
        String username = "user";
        String password = "1234567";
        return new AuthenticationRequest(username, password);
    }
    private AuthenticationRequest createAuthenticationRequestSuccess() {
        String username = "danibar";
        String password = "123456";
        return new AuthenticationRequest(username, password);
    }
    private UserRequest createUserRequestSuccess() {
        return UserRequest.builder()
                .nombres("Daniel")
                .correo("daniela@gmail.com")
                .telefono(BigInteger.valueOf(0000000000))
                .identificacion(BigInteger.valueOf(1111111111))
                .username("daniebar")
                .password("123456")
                .tipoDocumento("Cedula de ciudadania")
                .especialidades(Set.of("Civil"))
                .firmaId(1)
                .build();
    }
    private UserRequest createUserRequestFail() {
        return UserRequest.builder()
                .nombres("Daniel")
                .correo("daniela@gmail.com")
                .telefono(BigInteger.valueOf(0000000000))
                .identificacion(BigInteger.valueOf(1111111111))
                .username("danibar") //username already exists
                .password("123456")
                .tipoDocumento("Cedula de ciudadania")
                .especialidades(Set.of("Civil"))
                .firmaId(1)
                .build();
    }

    @Test
    void testCreateAbogado() {
    }

    @Test
    void testCreateJefe() {
    }

    @Test
    void testDeleteUser() {
    }
}