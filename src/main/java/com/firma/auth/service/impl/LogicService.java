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
    public String addJefe(UserResponse user) throws ErrorDataServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    urlData + "/add/jefe",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            return response.getBody();
        }catch (Exception e){
            throw new ErrorDataServiceException("Error al agregar el jefe");
        }
    }

    @Override
    public String addAbogado(UserResponse user) throws ErrorDataServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UserResponse> requestEntity = new HttpEntity<>(user, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    urlData + "/add/abogado",
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            return response.getBody();
        }catch (Exception e){
            throw new ErrorDataServiceException("Error al agregar el abogado");
        }
    }

    @Override
    public Role getRole(String username) throws ErrorDataServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Role> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Role> response = restTemplate.exchange(
                    urlData + "/rol/get/user?username="+username,
                    HttpMethod.GET,
                    requestEntity,
                    Role.class);
            return response.getBody();
        }catch (Exception e){
            throw new ErrorDataServiceException("Error al obtener el rol del usuario");
        }
    }
}
