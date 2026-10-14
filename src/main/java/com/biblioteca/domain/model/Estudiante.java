package com.biblioteca.domain.model;

/**
 * Representa a un estudiante universitario registrado en el sistema de biblioteca.
 * Un estudiante puede tener hasta 3 ejemplares en préstamo simultáneamente.
 */
public class Estudiante extends Usuario {

    /** Carrera o programa académico al que pertenece el estudiante. */
    private String carrera;

    /** Semestre actual que cursa el estudiante. */
    private int semestre;

    /** Constructor sin argumentos requerido por frameworks. */
    public Estudiante() {
        super();
    }

    /**
     * Constructor con todos los campos de Estudiante.
     *
     * @param idUsuario identificador único del usuario
     * @param nombre    nombre completo
     * @param carrera   carrera académica
     * @param semestre  semestre actual
     */
    public Estudiante(String idUsuario, String nombre, String carrera, int semestre) {
        super(idUsuario, nombre);
        this.carrera = carrera;
        this.semestre = semestre;
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }

    // ─── Regla de negocio polimórfica ─────────────────────────────────────────

    /**
     * Los estudiantes tienen un límite de 3 préstamos simultáneos.
     *
     * @return 3
     */
    @Override
    public int getLimitePrestamos() {
        return 3;
    }

    @Override
    public String toString() {
        return "Estudiante[id=" + getIdUsuario()
             + ", nombre=" + getNombre()
             + ", carrera=" + carrera
             + ", semestre=" + semestre + "]";
    }
}
