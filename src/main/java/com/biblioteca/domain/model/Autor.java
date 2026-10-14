package com.biblioteca.domain.model;

/**
 * Representa un autor literario asociado a uno o más libros del catálogo.
 */
public class Autor {

    /** Identificador único del autor en el sistema. */
    private String idAutor;

    /** Nombre completo del autor. */
    private String nombre;

    /** Breve descripción biográfica del autor. */
    private String biografia;

    public Autor() {}

    public Autor(String idAutor, String nombre, String biografia) {
        this.idAutor = idAutor;
        this.nombre = nombre;
        this.biografia = biografia;
    }

    // ─── Builder estático ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Autor autor = new Autor();
        public Builder idAutor(String v)   { autor.idAutor   = v; return this; }
        public Builder nombre(String v)    { autor.nombre    = v; return this; }
        public Builder biografia(String v) { autor.biografia = v; return this; }
        public Autor build() { return autor; }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getIdAutor()   { return idAutor;   }
    public void setIdAutor(String idAutor)     { this.idAutor   = idAutor;   }

    public String getNombre()    { return nombre;    }
    public void setNombre(String nombre)       { this.nombre    = nombre;    }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    @Override
    public String toString() {
        return "Autor[id=" + idAutor + ", nombre=" + nombre + "]";
    }
}
