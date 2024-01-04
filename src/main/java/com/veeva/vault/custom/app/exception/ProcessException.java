package com.veeva.vault.custom.app.exception;

/**
 * Thrown during script execution
 */
public class ProcessException extends java.lang.Exception {
    public ProcessException(String message){
        super(message);
    }
}
