package com.biblioteca.domain.model;

/**
 * Clase abstracta base que representa a cualquier usuario del sistema de biblioteca.
 * Aplica el Principio de Sustitución de Liskov (LSP): cualquier subclase puede
 * ser usada donde se espera un Usuario sin alterar el comportamiento del sistema.
 */
public abstract class Usuario {

    /** Identificador único del usuario en el sistema. */
    protected String idUsuario;

    /** Nombre completo del usuario. */
    protected String nombre;

    /** Constructor sin argumentos requerido por Spring y frameworks de serialización. */
    public Usuario() {}

    /**
     * Constructor con todos los campos del usuario base.
     *
     * @param idUsuario identificador único del usuario.
     * @param nombre    nombre completo del usuario.
     */
    public Usuario(String idUsuario, String nombre) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    // ─── Getters y Setters explícitos ────────────────────────────────────────

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // ─── Método abstracto del Template Method ─────────────────────────────────

    /**
     * Retorna el límite máximo de ejemplares que puede tener en préstamo simultáneamente.
     * Cada subtipo define su propia política de préstamo (LSP + Template Method).
     *
     * @return número máximo de préstamos permitidos.
     */
    public abstract int getLimitePrestamos();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + idUsuario + ", nombre=" + nombre + "]";
    }
}
