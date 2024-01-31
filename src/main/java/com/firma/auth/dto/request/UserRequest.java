package com.firma.auth.dto.request;

import lombok.*;

import java.math.BigInteger;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class UserRequest {
    private String nombres;
    private String correo;
    private BigInteger telefono;
    private BigInteger identificacion;
    private String username;
    private String password;
    private String tipoDocumento;
    private Set<String> especialidades;
    private Integer firmaId;
}
