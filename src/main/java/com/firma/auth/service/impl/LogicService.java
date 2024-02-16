package com.firma.auth.service.impl;

import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.service.intf.ILogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LogicService implements ILogicService {

    @Value( "${api.logic.url}")
    private String urlData;

    @Autowired
    private  RestTemplate restTemplate;

    @Override
    public String addAdmin(UserResponse user) throws ErrorDataServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    urlData + "/add/admin",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            return response.getBody();
        }catch (Exception e){
            throw new ErrorDataServiceException("Error al agregar el administrador");
        }
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
        ResponseEntity<Role> response = restTemplate.getForEntity(urlData + "/rol/get/user?username="+username, Role.class);
        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
           throw new ErrorDataServiceException("Error al obtener el rol del usuario");
        }
        return response.getBody();
    }
}
