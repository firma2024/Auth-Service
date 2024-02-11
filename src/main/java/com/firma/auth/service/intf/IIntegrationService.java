package com.firma.auth.service.intf;


import com.firma.auth.dto.Role;
import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.exception.ErrorDataServiceException;

public interface IIntegrationService {

    void addAdmin(UserResponse user) throws ErrorDataServiceException;

    void addAbogado(UserResponse user) throws ErrorDataServiceException;

    void addJefe(UserResponse user) throws ErrorDataServiceException;

    Role getRole(String username) throws ErrorDataServiceException;
}
