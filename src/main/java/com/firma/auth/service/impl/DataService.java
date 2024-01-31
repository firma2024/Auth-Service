package com.firma.auth.service.impl;

import com.firma.auth.dto.response.UserResponse;
import com.firma.auth.service.intf.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DataService implements IDataService {

    @Value("${data.service-url}")
    String url;
    private final RestTemplate restTemplate;
    @Autowired
    public DataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Override
    public void SendToDataComponent(UserResponse userResponse) {
        restTemplate.postForEntity(url, userResponse, UserResponse.class);
    }
}
