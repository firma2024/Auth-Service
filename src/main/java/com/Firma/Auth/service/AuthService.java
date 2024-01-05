package com.Firma.Auth.service;

import com.Firma.Auth.dto.AuthenticationRequest;
import com.Firma.Auth.tool.ObjectToUrlEncodedConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

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


    public String getAccessToken(AuthenticationRequest request) {
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
        return (String) responseMap.get("access_token");
    }
}
