package com.firma.auth.exception;

import lombok.Getter;
    /**
    * This class is used to handle exceptions that occur in the Keycloak service
    * It extends the Exception class and has a constructor that takes in a message and a status code
    * @see Exception
    * @see Getter
    */

@Getter
public class ErrorKeycloakServiceException extends Exception{
    private final int statusCode;
    public ErrorKeycloakServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}