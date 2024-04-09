package com.firma.auth.controller;


import com.firma.auth.dto.request.AuthenticationRequest;
import com.firma.auth.dto.request.UserRequest;
import com.firma.auth.dto.response.MessageResponse;
import com.firma.auth.dto.response.TokenResponse;
import com.firma.auth.exception.ErrorKeycloakServiceException;
import com.firma.auth.service.intf.IKeycloakService;
import com.firma.auth.tool.CryptoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirement(name = "Keycloak")
public class AuthController {
    private final IKeycloakService keycloakService;

    @Value("${api.rol.admin}")
    private String adminRole;
    @Value("${api.rol.jefe}")
    private String jefeRole;
    @Value("${api.rol.abogado}")
    private String abogadoRole;
    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    public AuthController(IKeycloakService keycloakService) {
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
    public ResponseEntity<?> getAccessToken(@RequestBody AuthenticationRequest request) throws ErrorKeycloakServiceException {
        String password = cryptoUtil.decrypt(request.getPassword());
        request.setPassword(password);
        TokenResponse token = keycloakService.getAccessToken(request);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    /**
     * Este metodo permite restaurar la contraseña de un usuario
     * @param username Nombre de usuario
     * @return Mensaje de confirmación ó mensaje de error
     */

    @Operation(summary = "Restaurar contraseña", description = "Envia un correo para restaurar la contraseña")
    @ApiResponse(responseCode = "200", description = "Correo enviado")
    @ApiResponse(responseCode = "404", description = "Error al enviar el correo")
    @Parameter(name = "username", description = "Nombre de usuario", required = true)
    @PostMapping("/{username}/forgot-password")
    public ResponseEntity<?> forgotPassword(@PathVariable String username) {
        ResponseEntity<?> response = keycloakService.forgotPassword(username);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(new MessageResponse("Correo enviado"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Este metodo permite crear un usuario con rol ADMIN
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     */

    @Operation(summary = "Crear un usuario con rol ADMIN", description = "Crea un usuario con rol ADMIN")
    @ApiResponse(responseCode = "201", description = "Admin creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el admin")
    @PostMapping("/admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserRequest userRequest) {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest, adminRole);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin creado");
        } else {
            return ResponseEntity.badRequest().body("Error creating admin");
        }
    }

    /**
     * Este metodo permite crear un usuario con rol JEFE
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     */

    @Operation(summary = "Crear un usuario con rol JEFE", description = "Crea un usuario con rol JEFE")
    @ApiResponse(responseCode = "201", description = "Jefe creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el jefe")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/jefe")
    public ResponseEntity<?> createJefe(@RequestBody UserRequest userRequest) {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest , jefeRole);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Jefe creado"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating jefe"));
        }
    }

    /**
     * Este metodo permite crear un usuario con rol ABOGADO
     * @param userRequest Datos del usuario a crear
     * @return Mensaje de confirmación ó mensaje de error
     */

    @Operation(summary = "Crear un usuario con rol ABOGADO", description = "Crea un usuario con rol ABOGADO")
    @ApiResponse(responseCode = "201", description = "Abogado creado")
    @ApiResponse(responseCode = "400", description = "Error al crear el abogado")
    @PreAuthorize("hasAnyAuthority('ADMIN' ,'JEFE')")
    @PostMapping("/abogado")
    public ResponseEntity<?> createAbogado(@RequestBody UserRequest userRequest) {
        ResponseEntity<?> response = keycloakService.createUserWithRole(userRequest, abogadoRole);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Abogado creado"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating abogado"));
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
                return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Usuario desabilitado"));
            else return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
