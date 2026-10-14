package com.biblioteca.infrastructure.persistence;

import com.biblioteca.domain.model.Ejemplar;
import com.biblioteca.domain.model.Libro;
import com.biblioteca.domain.port.LibroRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del {@link LibroRepository}.
 * Al guardar un libro, indexa también sus ejemplares por código de barras
 * para búsquedas directas O(1).
 */
@Repository
public class InMemoryLibroRepository implements LibroRepository {

    /** Mapa de libros indexados por ISBN. */
    private final Map<String, Libro> librosPorIsbn = new ConcurrentHashMap<>();

    /** Índice invertido: codigoBarras → Ejemplar (para búsquedas directas). */
    private final Map<String, Ejemplar> ejemplaresPorCodigo = new ConcurrentHashMap<>();

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        return Optional.ofNullable(librosPorIsbn.get(isbn));
    }

    @Override
    public Optional<Ejemplar> findEjemplarByCodigoBarras(String codigoBarras) {
        return Optional.ofNullable(ejemplaresPorCodigo.get(codigoBarras));
    }

    @Override
    public Libro save(Libro libro) {
        librosPorIsbn.put(libro.getIsbn(), libro);
        // Reindexar ejemplares del libro
        if (libro.getEjemplares() != null) {
            libro.getEjemplares().forEach(e ->
                ejemplaresPorCodigo.put(e.getCodigoBarras(), e));
        }
        return libro;
    }

    @Override
    public java.util.List<Libro> findAll() {
        return new java.util.ArrayList<>(librosPorIsbn.values());
    }
}
