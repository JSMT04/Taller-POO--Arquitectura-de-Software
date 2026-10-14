package com.biblioteca.domain.exception;

/**
 * Se lanza cuando se intenta crear un préstamo o reserva para un libro/ejemplar
 * que no tiene ejemplares disponibles para préstamo directo.
 */
public class LibroNoDisponibleException extends BibliotecaException {

    public LibroNoDisponibleException(String isbn) {
        super("El libro con ISBN '" + isbn + "' no tiene ejemplares disponibles para préstamo directo.");
    }

    public LibroNoDisponibleException(String codigoBarras, boolean esEjemplar) {
        super("El ejemplar con código '" + codigoBarras + "' no está disponible (estado actual: PRESTADO).");
    }
}
