package com.firma.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.exception.ErrorKeycloakServiceException;
import com.firma.auth.tool.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@WebAppConfiguration
class AuthControllerTest {

    private final static String AuthURL = "/api/auth";

    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    CryptoUtil cryptoUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Test the getAccessToken method
     *
     * @throws Exception if the request is not successful
     */

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

    /**
     * Create an AuthenticationRequest object with the necessary fields to succeed
     *
     * @return the AuthenticationRequest object
     */

    private AuthenticationRequest createAuthenticationRequestSuccess() {
        String username = "danibar";
        String password = "12345";
        password = cryptoUtil.encrypt(password);
        return new AuthenticationRequest(username, password);
    }

    /**
     * Test the getAccessToken method but the request needs to fail
     *
     * @throws Exception if the request is not successful
     */

    @Test
    void getAccessTokenFail() {
        try {
            AuthenticationRequest request = createAuthenticationRequestFail();
            mockMvc.perform(post(AuthURL + "/login")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(mapToJson(request))).andReturn();

        } catch (ErrorKeycloakServiceException e) {
            assertEquals(401, e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Create an AuthenticationRequest object with the necessary fields to fail
     *
     * @return the AuthenticationRequest object
     */

    private AuthenticationRequest createAuthenticationRequestFail() {
        String username = "user";
        String password = "12345678914711";
        password = cryptoUtil.encrypt(password);
        return new AuthenticationRequest(username, password);
    }

    /**
     * Test the forgotPassword method
     *
     * @throws Exception if the request is not successful
     */

    @Test
    void testForgotPasswordSuccess() throws Exception {
        String username = "danibar";
        MvcResult result = mockMvc.perform(post(AuthURL + "/{username}/forgot-password", username)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * Test the forgotPassword method but the request needs to fail
     *
     * @throws Exception if the request is not successful
     */

    @Test
    void testForgotPasswordFail() throws Exception {
        String username = "user";
        MvcResult result = mockMvc.perform(post(AuthURL + "/{username}/forgot-password", username)).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    /**
     * Test the createAdmin method
     *
     * @throws Exception if the request is not successful
     */


    @Test
    void testCreateAdminSuccess() throws Exception {
        UserRequest userRequest = createAdminSuccess();
        MvcResult result = mockMvc.perform(post(AuthURL + "/admin")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest admin object with the necessary fields to succeed
     * @return the UserRequest admin object
     */

    private UserRequest createAdminSuccess() {
        return UserRequest.builder()
                .nombres("Daniel")
                .correo("croco@gmail.com")
                .username("jairoEduardo")
                .password("123456")
                .build();
    }

    /**
     * Test the createAdmin method but the request needs to fail
     *
     * @throws Exception if the request is not successful
     */

    @Test
    void testCreateAdminFail() throws Exception {
        UserRequest userRequest = createAdminFail();
        MvcResult result = mockMvc.perform(post(AuthURL + "/admin")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest admin object with the necessary fields to fail
     *
     * @return the UserRequest admin object
     */

    private UserRequest createAdminFail() {
        return UserRequest.builder()
                .username("danibar")//username already exists
                .build();
    }

    /**
     * Test the createJefe method
     *
     * @throws Exception if the request is not successful
     */
    @Test
    void testCreateJefeSuccess() throws Exception {
        UserRequest userRequest = createJefeSuccess();
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("username", "password", AuthorityUtils.createAuthorityList("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(principal);
        MvcResult result = mockMvc.perform(post(AuthURL + "/jefe")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest jefe object with the necessary fields to succeed
     *
     * @return the UserRequest jefe object
     */

    private UserRequest createJefeSuccess() {
        return UserRequest.builder()
                .nombres("Daniel")
                .correo("koprada@javeriana.edu.co")
                .username("kevs")
                .password("123456")
                .build();
    }

    /**
     * Test the createJefe method but the request needs to fail
     *
     * @throws Exception if the request is not successful
     */
    @Test
    void testCreateJefeFail() throws Exception {
        UserRequest userRequest = CreateJefeFail();
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("username", "password", AuthorityUtils.createAuthorityList("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(principal);
        MvcResult result = mockMvc.perform(post(AuthURL + "/jefe")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest jefe object with the necessary fields to fail
     *
     * @return the UserRequest jefe object
     */

    private UserRequest CreateJefeFail() {
        return UserRequest.builder()
                .username("danibar")
                .password("123456")
                .build();
    }

    /**
     * Test the createAbogado method
     *
     * @throws Exception if the request is not successful
     */

    @Test
    void testCreateAbogadoSuccess() throws Exception {
        UserRequest userRequest = CreateAbogadoSuccess();
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("username", "password", AuthorityUtils.createAuthorityList("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(principal);
        MvcResult result = mockMvc.perform(post(AuthURL + "/abogado")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest abogado object with the necessary fields to succeed
     *
     * @return the UserRequest abogado object
     */

    private UserRequest CreateAbogadoSuccess() {
        return UserRequest.builder()
                .nombres("Carlos")
                .correo("carlosdesilvestrir@javeriana.edu.co")
                .username("CarlitosDS")
                .password("123456")
                .build();
    }

    /**
     * Test the createAbogado method but the request needs to fail
     *
     * @throws Exception if the request is not successful
     */


    @Test
    void testCreateAbogadoFail() throws Exception {
        UserRequest userRequest = CreateAbogadoFail();
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("username", "password", AuthorityUtils.createAuthorityList("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(principal);
        MvcResult result = mockMvc.perform(post(AuthURL + "/abogado")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapToJson(userRequest))).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    /**
     * Create a UserRequest abogado object with the necessary fields to fail
     *
     * @return the UserRequest abogado object
     */

    private UserRequest CreateAbogadoFail() {
        return UserRequest.builder()
                .nombres("Daniel")
                .username("danibar")
                .build();
    }

    /**
     * @param object to be converted to json
     * @return the object in json format
     * @throws JsonProcessingException if the object cannot be converted to json
     */

    private String mapToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}