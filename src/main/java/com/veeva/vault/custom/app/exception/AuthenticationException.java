package com.veeva.vault.custom.app.exception;

/**
 * Thrown when an Authentication Exception occurs, for example when attempting to access the Cache Context of a particular Vault DNS
 */
public class AuthenticationException extends Exception{
    public AuthenticationException(String message){
        super(message);
    }
}
