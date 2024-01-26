package com.firma.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String access_token;
    private String role;

}
