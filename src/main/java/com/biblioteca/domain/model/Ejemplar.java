package com.biblioteca.domain.model;

/**
 * Representa un ejemplar físico de un libro en la biblioteca.
 * Cada ejemplar tiene un código de barras único y un estado que refleja
 * su disponibilidad en el sistema. Encapsula la lógica de transición de estado.
 */
public class Ejemplar {

    /** Estados válidos para un ejemplar. */
    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_PRESTADO   = "PRESTADO";

    /** Código de barras único que identifica físicamente este ejemplar. */
    private String codigoBarras;

    /**
     * Estado actual del ejemplar.
     * Valores posibles: {@value #ESTADO_DISPONIBLE}, {@value #ESTADO_PRESTADO}.
     */
    private String estado;

    public Ejemplar() {}

    public Ejemplar(String codigoBarras, String estado) {
        this.codigoBarras = codigoBarras;
        this.estado = estado;
    }

    // ─── Builder estático ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Ejemplar e = new Ejemplar();
        public Builder codigoBarras(String v) { e.codigoBarras = v; return this; }
        public Builder estado(String v)       { e.estado       = v; return this; }
        public Ejemplar build() { return e; }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // ─── Lógica de negocio ────────────────────────────────────────────────────

    /**
     * Actualiza el estado del ejemplar a uno de los valores definidos.
     *
     * @param nuevoEstado el nuevo estado a asignar.
     * @throws IllegalArgumentException si el estado proporcionado no es válido.
     */
    public void actualizarEstado(String nuevoEstado) {
        if (!ESTADO_DISPONIBLE.equals(nuevoEstado) && !ESTADO_PRESTADO.equals(nuevoEstado)) {
            throw new IllegalArgumentException(
                "Estado inválido: '" + nuevoEstado + "'. Los estados válidos son: "
                + ESTADO_DISPONIBLE + ", " + ESTADO_PRESTADO);
        }
        this.estado = nuevoEstado;
    }

    /**
     * Indica si el ejemplar está disponible para ser prestado.
     *
     * @return {@code true} si el estado es "DISPONIBLE".
     */
    public boolean isDisponible() {
        return ESTADO_DISPONIBLE.equals(this.estado);
    }

    @Override
    public String toString() {
        return "Ejemplar[codigoBarras=" + codigoBarras + ", estado=" + estado + "]";
    }
}
