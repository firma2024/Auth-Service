package com.firma.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatingUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String userName;
    private String password;
    private String telefono;
    private String identificacion;
    private String tipoDocumento;
    private List<String> especialidades;
    private int firmaId;

}
