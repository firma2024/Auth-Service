package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.service.intf.IIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class IntegrationService implements IIntegrationService {

    @Value( "${api.integration.url}")
    String urlData;

    private final RestTemplate restTemplate;
    @Autowired
    public IntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public void addAdmin(UserResponse user) throws ErrorDataServiceException {
        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "/add/admin", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el administrador");
        }
        response.getBody();
    }

    @Override
    public void addAbogado(UserResponse user) throws ErrorDataServiceException {

        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "/add/abogado", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el abogado");
        }
        response.getBody();
    }

    @Override
    public void addJefe(UserResponse user) throws ErrorDataServiceException {
        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "/add/jefe", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el jefe");
        }
        response.getBody();
    }

    @Override
    public Role getRole(String username) throws ErrorDataServiceException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlData + "/rol/get/user")
                .queryParam("username", username);
        ResponseEntity<Role> response = restTemplate.getForEntity(builder.toUriString(), Role.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
           throw new ErrorDataServiceException("Error al obtener el rol del usuario");
        }
        return response.getBody();
    }
}
