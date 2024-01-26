package com.firma.auth.dto;

import lombok.*;

import java.util.List;
import java.util.Objects;


@Setter
@Getter
@NoArgsConstructor
public class User {
    private String id;
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

    public User(String firstName, String lastName, String email, String userName, String password,
                String telefono, String identificacion, String tipoDocumento, List<String> especialidades, int firmaId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.telefono = telefono;
        this.identificacion = identificacion;
        this.tipoDocumento = tipoDocumento;
        this.especialidades = especialidades;
        this.firmaId = firmaId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", telefono='" + telefono + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", especialidades=" + especialidades +
                ", firmaId=" + firmaId +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
