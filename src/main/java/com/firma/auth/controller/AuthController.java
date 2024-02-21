package com.firma.auth.controller;


import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.exception.ErrorDataServiceException;
import com.firma.auth.service.impl.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirement(name = "Keycloak")
public class AuthController {
    private final KeycloakService keycloakService;

    @Autowired
    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    /**
     * Obtiene el token de acceso al sistema
     * @param request Datos de autenticación (username y password)
     * @return Token de acceso
     */

    @Operation(summary = "Obtener el token de acceso al sistema", description = "Obtiene el token")
    @ApiResponse(responseCode = "200", description = "Token obtenido")
    @ApiResponse(responseCode = "400", description = "Error al obtener el token de acceso")
    @PostMapping("/login")
    public ResponseEntity<?> getAccessToken(@RequestBody AuthenticationRequest request) {
        try {
            TokenResponse token = keycloakService.getAccessToken(request);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (ErrorDataServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Este metodo permite restaurar la contraseña de un usuario
     * @param username Nombre de usuario
     * @return Mensaje de confirmación ó mensaje de error
     * @throws ErrorDataServiceException Error al enviar el correo
     */

    @Operation(summary = "Restaurar contraseña", description = "Envia un correo para restaurar la contraseña")
    @ApiResponse(responseCode = "200", description = "Correo enviado")
    @ApiResponse(responseCode = "404", description = "Error al enviar el correo")
    @Parameter(name = "username", description = "Nombre de usuario", required = true)
    @PostMapping("/{username}/forgot-password")
    public ResponseEntity<?> forgotPassword(@PathVariable String username) throws ErrorDataServiceException {
        ResponseEntity<?> response = keycloakService.forgotPassword(username);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.OK).body("Correo enviado");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error al enviar el correo");
        }
    }

    /**
     * Este metodo permite crear un usuario con rol ADMIN
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     * @throws ErrorDataServiceException Error al crear el ADMIN
     */

    @Operation(summary = "Crear un usuario con rol ADMIN", description = "Crea un usuario con rol ADMIN")
    @ApiResponse(responseCode = "201", description = "Admin creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el admin")
    @PostMapping("/admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserRequest userRequest) throws ErrorDataServiceException {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest, "ADMIN");
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin creado");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear el admin");
        }
    }

    /**
     * Este metodo permite crear un usuario con rol JEFE
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     * @throws ErrorDataServiceException Error al crear el JEFE
     */

    @Operation(summary = "Crear un usuario con rol JEFE", description = "Crea un usuario con rol JEFE")
    @ApiResponse(responseCode = "201", description = "Jefe creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el jefe")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/jefe")
    public ResponseEntity<?> createJefe(@RequestBody UserRequest userRequest) throws ErrorDataServiceException {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest , "JEFE");
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Jefe created");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating jefe");
        }
    }

    /**
     * Este metodo permite crear un usuario con rol ABOGADO
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     * @throws ErrorDataServiceException Error al crear el ABOGADO
     */

    @Operation(summary = "Crear un usuario con rol ABOGADO", description = "Crea un usuario con rol ABOGADO")
    @ApiResponse(responseCode = "201", description = "Abogado creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el abogado")
    @PreAuthorize("hasAnyAuthority('ADMIN' ,'JEFE')")
    @PostMapping("/abogado")
    public ResponseEntity<?> createAbogado(@RequestBody UserRequest userRequest) throws ErrorDataServiceException {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest, "ABOGADO");
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Abogado created");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating abogado");
        }
    }

    /**
     * Este metodo permite desabilitar un usuario
     * @param id Identificador del usuario
     * @return Mensaje de confirmación ó mensaje de error
     */

    @Operation(summary = "Desabilitar un usuario", description = "Desabilita un usuario")
    @ApiResponse(responseCode = "200", description = "Usuario desabilitado")
    @ApiResponse(responseCode = "404", description = "Error al desabilitar el usuario")
    @Parameter(name = "id", description = "Identificador del usuario", required = true)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        try {
            if (keycloakService.deleteAccount(id))
                return ResponseEntity.status(HttpStatus.OK).body("User deleted");
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
