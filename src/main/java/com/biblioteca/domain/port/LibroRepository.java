package com.biblioteca.domain.port;

import com.biblioteca.domain.model.Ejemplar;
import com.biblioteca.domain.model.Libro;

import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de libros y ejemplares.
 */
public interface LibroRepository {

    /**
     * Busca un libro por su ISBN.
     *
     * @param isbn el ISBN del libro.
     * @return un {@link Optional} con el libro si existe.
     */
    Optional<Libro> findByIsbn(String isbn);

    /**
     * Busca un ejemplar por su código de barras.
     *
     * @param codigoBarras el código de barras del ejemplar.
     * @return un {@link Optional} con el ejemplar si existe.
     */
    Optional<Ejemplar> findEjemplarByCodigoBarras(String codigoBarras);

    /**
     * Guarda o actualiza un libro en el repositorio.
     *
     * @param libro el libro a persistir.
     * @return el libro persistido.
     */
    Libro save(Libro libro);

    /**
     * Retorna todos los libros registrados en el catálogo.
     *
     * @return lista de todos los libros.
     */
    java.util.List<Libro> findAll();
}
