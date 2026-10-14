package com.biblioteca.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un título bibliográfico en el catálogo de la biblioteca.
 * Un libro puede tener múltiples ejemplares físicos y uno o más autores.
 * Encapsula la lógica de disponibilidad directamente en el dominio.
 */
public class Libro {

    /** ISBN (International Standard Book Number) del libro. */
    private String isbn;

    /** Título completo de la obra. */
    private String titulo;

    /**
     * Lista de ejemplares físicos asociados a este título.
     * Se inicializa vacía para evitar NullPointerException.
     */
    private List<Ejemplar> ejemplares = new ArrayList<>();

    /** Lista de autores que participaron en la creación de esta obra. */
    private List<Autor> autores = new ArrayList<>();

    public Libro() {}

    public Libro(String isbn, String titulo, List<Ejemplar> ejemplares, List<Autor> autores) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.ejemplares = ejemplares != null ? ejemplares : new ArrayList<>();
        this.autores    = autores    != null ? autores    : new ArrayList<>();
    }

    // ─── Builder estático ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Libro l = new Libro();
        public Builder isbn(String v)              { l.isbn      = v; return this; }
        public Builder titulo(String v)            { l.titulo    = v; return this; }
        public Builder ejemplares(List<Ejemplar> v){ l.ejemplares = v; return this; }
        public Builder autores(List<Autor> v)      { l.autores   = v; return this; }
        public Libro build() { return l; }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getIsbn()   { return isbn;   }
    public void setIsbn(String isbn)     { this.isbn   = isbn;   }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public List<Ejemplar> getEjemplares() { return ejemplares; }
    public void setEjemplares(List<Ejemplar> ejemplares) {
        this.ejemplares = ejemplares != null ? ejemplares : new ArrayList<>();
    }

    public List<Autor> getAutores() { return autores; }
    public void setAutores(List<Autor> autores) {
        this.autores = autores != null ? autores : new ArrayList<>();
    }

    // ─── Lógica de negocio ────────────────────────────────────────────────────

    /**
     * Evalúa si este título tiene al menos un ejemplar disponible para préstamo.
     *
     * @return {@code true} si al menos un ejemplar tiene estado "DISPONIBLE".
     */
    public boolean estaDisponible() {
        if (ejemplares == null || ejemplares.isEmpty()) {
            return false;
        }
        return ejemplares.stream().anyMatch(Ejemplar::isDisponible);
    }

    /**
     * Retorna el primer ejemplar disponible para préstamo, si existe.
     *
     * @return el primer {@link Ejemplar} disponible, o {@code null} si no hay ninguno.
     */
    public Ejemplar getPrimerEjemplarDisponible() {
        if (ejemplares == null) return null;
        return ejemplares.stream()
                .filter(Ejemplar::isDisponible)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Libro[isbn=" + isbn + ", titulo=" + titulo + "]";
    }
}
