package com.biblioteca.domain.exception;

/**
 * Se lanza cuando se intenta acceder a un recurso (usuario, libro, ejemplar,
 * préstamo, reserva) que no existe en el repositorio.
 */
public class RecursoNoEncontradoException extends BibliotecaException {

    public RecursoNoEncontradoException(String tipoRecurso, String identificador) {
        super(String.format("No se encontró el recurso '%s' con identificador '%s'.",
            tipoRecurso, identificador));
    }
}
