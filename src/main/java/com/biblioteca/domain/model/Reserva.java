package com.biblioteca.domain.model;

import java.util.Date;

/**
 * Representa una reserva de un libro para un usuario cuando todos sus ejemplares
 * están prestados.
 */
public class Reserva {

    /** Estados válidos para una reserva. */
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CUMPLIDA  = "CUMPLIDA";
    public static final String ESTADO_CANCELADA = "CANCELADA";

    /** Identificador único de la reserva. */
    private String idReserva;

    /** Fecha y hora en que se registró la reserva. */
    private Date fechaReserva;

    /** Estado actual de la reserva. */
    private String estado = ESTADO_PENDIENTE;

    /** Usuario que realizó la reserva. */
    private Usuario usuario;

    /** Libro reservado (título bibliográfico). */
    private Libro libro;

    public Reserva() {}

    // ─── Builder estático ────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Reserva r = new Reserva();
        public Builder idReserva(String v)   { r.idReserva   = v; return this; }
        public Builder fechaReserva(Date v)  { r.fechaReserva= v; return this; }
        public Builder estado(String v)      { r.estado       = v; return this; }
        public Builder usuario(Usuario v)    { r.usuario      = v; return this; }
        public Builder libro(Libro v)        { r.libro        = v; return this; }
        public Reserva build() { return r; }
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getIdReserva()            { return idReserva;    }
    public void   setIdReserva(String v)    { this.idReserva   = v;}

    public Date getFechaReserva()           { return fechaReserva; }
    public void setFechaReserva(Date v)     { this.fechaReserva= v;}

    public String getEstado()               { return estado;       }
    public void   setEstado(String v)       { this.estado      = v;}

    public Usuario getUsuario()             { return usuario;      }
    public void    setUsuario(Usuario v)    { this.usuario     = v;}

    public Libro getLibro()                 { return libro;        }
    public void  setLibro(Libro v)          { this.libro       = v;}

    @Override
    public String toString() {
        return "Reserva[id=" + idReserva
             + ", estado=" + estado
             + ", usuario=" + (usuario != null ? usuario.getNombre() : "null")
             + ", libro=" + (libro != null ? libro.getTitulo() : "null") + "]";
    }
}
