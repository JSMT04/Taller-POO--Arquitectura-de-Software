package com.biblioteca.domain.exception;

/**
 * Excepción base del dominio de la biblioteca.
 * Todas las excepciones específicas del negocio extienden de esta clase,
 * permitiendo capturas selectivas por capas.
 */
public class BibliotecaException extends RuntimeException {

    public BibliotecaException(String message) {
        super(message);
    }

    public BibliotecaException(String message, Throwable cause) {
        super(message, cause);
    }
}
