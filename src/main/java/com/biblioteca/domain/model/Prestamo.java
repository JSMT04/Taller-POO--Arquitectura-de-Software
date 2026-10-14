package com.biblioteca.domain.model;

import com.biblioteca.domain.port.ServicioCalendario;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Representa un préstamo de ejemplares realizado por un usuario de la biblioteca.
 * Encapsula la lógica de cálculo de multas utilizando el servicio de calendario
 * inyectado, aplicando el Principio de Inversión de Dependencias (DIP).
 *
 * <p>Tarifa de multa: $2.500 COP por cada día hábil de retraso por ejemplar prestado.</p>
 */
public class Prestamo {

    /** Tarifa en COP por día hábil de retraso por ejemplar. */
    public static final double TARIFA_MULTA_POR_DIA_HABIL = 2_500.0;

    /** Identificador único del préstamo. */
    private String idPrestamo;

    /**
     * Fecha en que se realizó el préstamo (fecha de salida).
     * También se usa como fecha de devolución real cuando se procesa la devolución.
     */
    private Date fechaEntrega;

    /**
     * Fecha límite pactada para la devolución de los ejemplares.
     */
    private Date fechaDevolucion;

    /** Estado del préstamo: "ACTIVO" o "DEVUELTO". */
    private String estado = "ACTIVO";

    /** Usuario al que se le realizó el préstamo. */
    private Usuario usuario;

    /** Lista de ejemplares físicos incluidos en este préstamo. */
    private List<Ejemplar> ejemplares = new ArrayList<>();

    public Prestamo() {}

    // ─── Builder estático ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Prestamo p = new Prestamo();
        public Builder idPrestamo(String v)        { p.idPrestamo     = v; return this; }
        public Builder fechaEntrega(Date v)        { p.fechaEntrega   = v; return this; }
        public Builder fechaDevolucion(Date v)     { p.fechaDevolucion= v; return this; }
        public Builder estado(String v)            { p.estado         = v; return this; }
        public Builder usuario(Usuario v)          { p.usuario        = v; return this; }
        public Builder ejemplares(List<Ejemplar> v){ p.ejemplares     = v; return this; }
        public Prestamo build() { return p; }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getIdPrestamo()            { return idPrestamo; }
    public void   setIdPrestamo(String v)    { this.idPrestamo     = v; }

    public Date getFechaEntrega()            { return fechaEntrega; }
    public void setFechaEntrega(Date v)      { this.fechaEntrega   = v; }

    public Date getFechaDevolucion()         { return fechaDevolucion; }
    public void setFechaDevolucion(Date v)   { this.fechaDevolucion = v; }

    public String getEstado()                { return estado; }
    public void   setEstado(String v)        { this.estado         = v; }

    public Usuario getUsuario()              { return usuario; }
    public void    setUsuario(Usuario v)     { this.usuario        = v; }

    public List<Ejemplar> getEjemplares()            { return ejemplares; }
    public void           setEjemplares(List<Ejemplar> v) {
        this.ejemplares = v != null ? v : new ArrayList<>();
    }

    // ─── Lógica de negocio ────────────────────────────────────────────────────

    /**
     * Calcula la multa por retraso en la devolución de los ejemplares.
     * Cuenta únicamente los días hábiles del periodo de mora.
     *
     * @param calendario servicio que determina si una fecha es día hábil laboral.
     * @return el monto total de la multa en COP; {@code 0.0} si no hay retraso.
     */
    public double calcularMulta(ServicioCalendario calendario) {
        if (fechaEntrega == null || fechaDevolucion == null) {
            return 0.0;
        }

        // Si la devolución fue a tiempo o antes, no hay multa
        if (!fechaEntrega.after(fechaDevolucion)) {
            return 0.0;
        }

        // Iterar día a día desde el día siguiente a la fecha límite hasta la devolución real
        int diasHabilesRetraso = 0;
        Calendar cursor = Calendar.getInstance();
        cursor.setTime(fechaDevolucion);
        cursor.add(Calendar.DATE, 1); // día siguiente a la fecha límite

        Calendar limiteCalendar = Calendar.getInstance();
        limiteCalendar.setTime(fechaEntrega);

        while (!cursor.getTime().after(limiteCalendar.getTime())) {
            if (calendario.esDiaHabil(cursor.getTime())) {
                diasHabilesRetraso++;
            }
            cursor.add(Calendar.DATE, 1);
        }

        int cantidadEjemplares = (ejemplares != null && !ejemplares.isEmpty())
            ? ejemplares.size() : 1;
        return diasHabilesRetraso * TARIFA_MULTA_POR_DIA_HABIL * cantidadEjemplares;
    }

    /**
     * Indica si el préstamo tiene retraso respecto a la fecha límite.
     *
     * @return {@code true} si la fecha de entrega real supera la fecha límite.
     */
    public boolean tieneRetraso() {
        return fechaEntrega != null
                && fechaDevolucion != null
                && fechaEntrega.after(fechaDevolucion);
    }

    @Override
    public String toString() {
        return "Prestamo[id=" + idPrestamo
             + ", estado=" + estado
             + ", usuario=" + (usuario != null ? usuario.getNombre() : "null") + "]";
    }
}
