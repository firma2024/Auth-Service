package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.service.intf.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DataService implements IDataService {

    @Value( "${api.data.url}")
    String urlData;

    private final RestTemplate restTemplate;
    @Autowired
    public DataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public String addAdmin(UserResponse user) throws ErrorDataServiceException {
        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "usuario/add/admin", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el administrador");
        }
        return response.getBody();
    }

    @Override
    public String addAbogado(UserResponse user) throws ErrorDataServiceException {

        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "usuario/add/abogado", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el abogado");
        }
        return response.getBody();
    }

    @Override
    public String addJefe(UserResponse user) throws ErrorDataServiceException {
        ResponseEntity<String> response = restTemplate.postForEntity(urlData + "usuario/add/jefe", user, String.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new ErrorDataServiceException("Error al agregar el jefe");
        }
        return response.getBody();
    }

    @Override
    public Role getRole(String username) throws ErrorDataServiceException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(urlData + "rol/get/user")
                .queryParam("username", username);
        ResponseEntity<Role> response = restTemplate.getForEntity(builder.toUriString(), Role.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
           throw new ErrorDataServiceException("Error al obtener el rol del usuario");
        }
        return response.getBody();
    }
}
