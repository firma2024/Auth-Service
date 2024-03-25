package com.firma.auth.dto.request;

import com.firma.auth.model.TipoAbogado;
import com.firma.auth.model.TipoDocumento;
import lombok.*;

import java.math.BigInteger;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRequest {
    private Integer id;
    private String nombres;
    private String correo;
    private BigInteger telefono;
    private BigInteger identificacion;
    private String password = "12345";
    private String username;
    private TipoDocumento tipoDocumento;
    private Set<TipoAbogado> especialidades;
    private Integer firmaId;
}
