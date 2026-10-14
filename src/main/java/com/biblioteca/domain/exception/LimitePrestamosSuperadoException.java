package com.biblioteca.domain.exception;

/**
 * Se lanza cuando un usuario intenta solicitar un préstamo pero ya ha alcanzado
 * el límite máximo de ejemplares permitidos según su tipo de usuario.
 */
public class LimitePrestamosSuperadoException extends BibliotecaException {

    public LimitePrestamosSuperadoException(String idUsuario, int limitePrestamos, int prestamosActuales) {
        super(String.format(
            "El usuario '%s' ha superado su límite de préstamos. Límite: %d, Activos: %d.",
            idUsuario, limitePrestamos, prestamosActuales
        ));
    }
}
