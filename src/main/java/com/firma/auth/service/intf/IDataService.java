package com.firma.auth.service.intf;


import com.firma.auth.dto.response.UserResponse;

public interface IDataService {
    void SendToDataComponent(UserResponse user);
}
