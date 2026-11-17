package com.kevdev.fantasymvp.exception;

public class EmailYaExistenteException extends RuntimeException {
    public EmailYaExistenteException() { super("El email ya existente");   }
}
