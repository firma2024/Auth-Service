package com.firma.auth.dto;

import java.util.List;
import java.util.Objects;

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

    public User() {
        // Constructor vacío
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public int getFirmaId() {
        return firmaId;
    }

    public void setFirmaId(int firmaId) {
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
