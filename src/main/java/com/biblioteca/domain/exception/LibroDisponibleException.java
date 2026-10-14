package com.biblioteca.domain.exception;

/**
 * Se lanza cuando se intenta crear una reserva para un libro que aún tiene
 * ejemplares disponibles. En ese caso no es necesario reservar; se puede
 * solicitar el préstamo directamente.
 */
public class LibroDisponibleException extends BibliotecaException {

    public LibroDisponibleException(String isbn) {
        super("El libro con ISBN '" + isbn + "' tiene ejemplares disponibles. "
            + "Puede solicitar el préstamo directamente sin necesidad de reserva.");
    }
}
